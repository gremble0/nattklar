package com.example.nattklar.model.dataprocessing

import android.content.Context
import com.example.nattklar.model.*
import com.example.nattklar.model.dataobjects.ConstellationData
import com.example.nattklar.model.dataobjects.Datatime
import com.example.nattklar.model.dataobjects.RightAscension
import com.example.nattklar.model.dataobjects.StarData
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.tan
import kotlin.math.sin
import kotlin.math.cos

/**
 * Responsible for retrieval and processing of astronomical data, such as stars and constellations.
 * It makes outgoing API requests to sunrise 3.0.
 */
object Astronomy {
    private val json = Json { ignoreUnknownKeys = true }

    private var stars: HashMap<String, StarData> = hashMapOf()
    private var constellations: HashMap<String, List<String>> = hashMapOf()
    private var coords: LatLng? = null
    private var coordsSunProperties: JSONObject? = null

    /**
     *  Loads data for stars, constellations and sunrise times for some selected cities.
     */
    suspend fun loadAstronomicData(context: Context) {
        // ensure data is not being loaded if it already has been
        if (stars.size > 0) return

        // load data
        loadStarData(context)
        loadConstellationData(context)
    }

    /**
     *  Loads star data from a json file containing star data of more than 1200 stars collected in
     *  advance.
     */
    private suspend fun loadStarData(context: Context) {
        val jsonString = DataNavigator.readJsonAsset(context, JsonAsset.Stars)
        val starsList: List<StarData> = json.decodeFromString(jsonString)

        // put the star data in a hash map for easier access
        for (star in starsList) {
            stars[star.name] = star
        }
    }

    /**
     *  Loads constellation data from a json file containing 41 constellations and their (up to) 30
     *  first stars with respect to relative magnitude.
     */
    private suspend fun loadConstellationData(context: Context) {
        val jsonString = DataNavigator.readJsonAsset(context, JsonAsset.Constellations)
        val constellationsList: List<ConstellationData> = json.decodeFromString(jsonString)

        // put the constellation data in a hash map for easier access
        for (constellation in constellationsList) {
            constellations[constellation.name] = constellation.stars
        }
    }

    /**
     *  Loads sunrise time for some selected cities through some API-requests. Purpose is improve
     *  performance by not avoiding the same API-requests repeated over and over.
     */
    suspend fun loadCoordinatesData(newCoords : LatLng) : JSONObject? {

        if (coords == newCoords) return coordsSunProperties

        coords = newCoords

        // store the sunrise times to avoid reduced performance from repeating the same API-requests
        val rawSunriseData = getRawSunriseAPIData(newCoords.latitude, newCoords.longitude)
        coordsSunProperties = rawSunriseData

        return coordsSunProperties
    }

    /**
     * @return [JSONObject] with raw sunrise data to a [latitude] and
     * [longitude] of current date.
     */
    private suspend fun getRawSunriseAPIData(latitude: Double, longitude: Double): JSONObject? {
        val date = getDate(getCurrentTime())

        val body = DataNavigator.getRawDataFromAPI(
            API.SunriseSun, latitude.toString(), longitude.toString(), date
        )
        return try {
            JSONObject(body as String)
        } catch (exception: Exception) {
            null
        }
    }

    /**
     * @return the general qualitative visibility description of a [constellationName] in Norway.
     */
    suspend fun visibilityOfConstellation(constellationName: String, coords : LatLng): Double {
        val stars = constellations[constellationName] ?: return 0.0

        var sum = 0.0
        for (starName in stars) {
            sum += visibilityOfStar(starName, coords)
        }

        return sum/stars.size
    }

    /**
     * @return the general qualitative visibility description of a [starName] in Norway.
     */
    private suspend fun visibilityOfStar(
        starName: String,
        newCoords : LatLng
    ): Double {
        if (newCoords != coords) {
            loadCoordinatesData(newCoords)
        }
        val star = stars[starName] ?: return 0.0

        // get a list of the visibility in percentage for some selected cities
        return percentageOfHoursStarIsVisible(star)
    }

    /**
     * @return the percentage of remaining night hours a [star] is visible.
     */
    private fun percentageOfHoursStarIsVisible(star: StarData): Double {

        if (coordsSunProperties == null) return 0.0

        // get sun properties
        val sunsetTimeString = coordsSunProperties!!.getJSONObject("properties").getJSONObject("sunset").optString("time", "null")
        val sunriseTimeString = coordsSunProperties!!.getJSONObject("properties").getJSONObject("sunrise").optString("time", "null")
        val solarNoonAltitude =
            coordsSunProperties!!.getJSONObject("properties").getJSONObject("solarnoon").optString("disc_centre_elevation", "null")

        // return hours of visibility for missing or empty values
        val maxVisibleHours = timeInHoursAStarIsVisible(coords!!, star)

        if (solarNoonAltitude == "null") return 0.0
        if (maxVisibleHours < 1) return 0.0
        if ((sunsetTimeString == "null" || sunriseTimeString == "null") && solarNoonAltitude.toFloat() < 0) return 0.0
        if ((sunsetTimeString == "null" || sunriseTimeString == "null") && solarNoonAltitude.toFloat() >= 0) return maxVisibleHours

        // prepare hourly iteration
        val sunsetTime = parseToUTC(sunsetTimeString)
        val sunriseTime = plusDaysOfDatetime(parseToUTC(sunriseTimeString), 1)

        var relevantTime = sunsetTime
        val nightHour = getHoursIntoDay(relevantTime)

        // check hour by hour whether the star is visible or not
        var totalHours = 0
        var visibleHours = 0
        while (true) {

            // update the time each hour
            if ((nightHour + totalHours) % 24 == 0) relevantTime =
                plusDaysOfDatetime(relevantTime, 1)
            val hourlyNightTime =
                getDatetimeWithChangedHour(relevantTime, (nightHour + totalHours) % 24)

            // remove times before the sunrise time
            if (hourlyNightTime > sunriseTime) break

            // calculate angle of altitude of star
            val altitudeOfStar = angleOfAltitudeOfStar(hourlyNightTime, coords!!, star)

            // update counters
            if (altitudeOfStar > 0) visibleHours++
            totalHours++
        }

        return visibleHours / totalHours.toDouble()
    }

    /**
     * @return the time in hours a [star] is visible for some [cords].
     */
    private fun timeInHoursAStarIsVisible(cords: LatLng, star: StarData): Double {
        val declination = star.declination.degrees.toDouble()

        return 0.13333 * (180 - acos(tan(cords.latitude) * tan(declination)))
    }

    /**
     * @return the Greenwich Mean Sidereal Time from a [datetime].
     */
    private fun convertToGMST(datetime: Datatime): Double { // Greenwich Mean Sidereal Time
        // 2451545.0 is midday of 01.01.2000, a reference date
        val residualTime = calculateJulianTime(datetime) - 2451545.0

        // The fraction of the century elapsed since the reference date above
        val t: Double = residualTime / 36525

        // Greenwich mean sidereal time
        return 280.46061837 + 360.98564736629 * residualTime + 0.00387933 * t.pow(2) - (1 / 38710000) * t.pow(
            3
        )
    }

    /**
     * @return the Local Mean Sidereal Time from a [datetime] and [longitude].
     */
    private fun convertToLMST(datetime: Datatime, longitude: Double): Double =
        convertToGMST(datetime) + longitude / 15.0

    /**
     * @return the [star] hour angle at a [datetime] and [cords].
     */
    private fun hourAngle(datetime: Datatime, star: StarData, cords: LatLng): Double {
        val lmst = convertToLMST(
            datetime,
            cords.longitude
        ) // the observer's exact astronomical measure of longitude
        val ra = getRightAscensionInSeconds(star.rightAscension) // the star's exact astronomical measure of longitude

        // the angle between the star and earth's horizon multiplied with 360 degrees (full circle) divided by 24 hours (hours in a day)
        return (lmst - ra) * (360 / 24)
    }

    /**
     * @return the [star] altitude angle at a [datetime] and [cords].
     */
    private fun angleOfAltitudeOfStar(datetime: Datatime, cords: LatLng, star: StarData): Double {
        val d = star.declination.degrees.toDouble()
        val h = hourAngle(datetime, star, cords) // hour angle of star

        return asin(sin(cords.latitude) * sin(d) + cos(cords.latitude) * cos(d) * cos(h))
    }

    /**
     * @return the julian date of a given [year], [month] and [day].
     */
    private fun calculateJulianDate(year: Int, month: Int, day: Int): Double {
        val a = 1461.0 * year + 4800.0 + (month - 14.0) / 12.0
        val b = 367.0 * (month - 2.0 - 12.0 * (month - 14.0) / 12.0)
        val c = 3.0 * (year + 4900.0 + (month - 14.0) / 12.0) / 100.0

        return a / 4.0 + b / 12.0 - c / 4.0 + day - 32075.0
    }

    /**
     * @return the julian time of a given [year], [month], [day], [hour], [minute] and [second].
     */
    private fun calculateJulianTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Double {
        return calculateJulianDate(
            year,
            month,
            day
        ) + (hour - 12.0) / 24.0 + minute / 1440.0 + second / 86400.0
    }

    /**
     * @return the julian time of a given [datetime].
     */
    private fun calculateJulianTime(datetime: Datatime): Double {
        val time = datetime.time
        return calculateJulianTime(
            time.year, time.monthValue, time.dayOfMonth, time.hour, time.minute, time.second
        )
    }

    /**
     * @return in seconds the [rightAscension] of some astronomical object.
     */
    private fun getRightAscensionInSeconds(rightAscension: RightAscension): Double {
        return rightAscension.hours * 3600 + rightAscension.minutes * 60 + rightAscension.seconds
    }
}