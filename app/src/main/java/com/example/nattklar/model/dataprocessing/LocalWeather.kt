package com.example.nattklar.model.dataprocessing


import com.example.nattklar.model.dataobjects.*
import com.example.nattklar.model.API
import com.example.nattklar.model.DataNavigator
import com.example.nattklar.model.getCurrentTime
import com.example.nattklar.model.getDate
import com.example.nattklar.model.getHoursBetween
import com.example.nattklar.model.getHoursIntoDay
import com.example.nattklar.model.getNightNumber
import com.example.nattklar.model.parseToUTC
import com.example.nattklar.model.plusDaysOfDatetime
import com.example.nattklar.model.dataobjects.AirQualityDataAtTime
import com.example.nattklar.model.dataobjects.DataAtLocationForNight
import com.example.nattklar.model.dataobjects.Datatime
import com.example.nattklar.model.dataobjects.ForecastAtLocation
import com.example.nattklar.model.dataobjects.LocationDescription
import com.example.nattklar.model.dataobjects.Night
import com.example.nattklar.model.dataobjects.NightConditionsSummary
import com.example.nattklar.model.dataobjects.SolarProperties
import com.example.nattklar.model.dataobjects.WeatherDataAtTime
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.util.*

/**
 * Responsible for collecting relevant weather and solar data from various API sources, and put
 * it into data structures easy to use.
 */
object LocalWeather {
    /**
     * @return JSONObject with raw data from a HTTP-response provided by [API] with queries: [cords]
     * and [date].
     */
    private suspend fun getRawLocalData(
        api: API,
        cords: LatLng,
        date: String? = null
    ): JSONObject? {
        // get raw data from an API request
        val body = DataNavigator.getRawDataFromAPI(
            api, cords.latitude.toString(), cords.longitude.toString(), date.toString()
        )

        // convert the API data to either as JSON object or null
        return try {
            JSONObject(body as String)
        } catch (exception: Exception) {
            null
        }
    }

    /**
     * @return short- and longterm series of temperature, wind speed, cloud area fraction and air
     * quality data wrapped with sunset time, for certain [cords].
     */
    suspend fun getLocalData(location: String, cords: LatLng): ForecastAtLocation? {
        val rawWeatherData = getRawLocalData(API.LocationForecast, cords)
        val rawAirQualityData = getRawLocalData(API.AirQuality, cords)

        if (rawWeatherData == null) return null

        val weatherDatas = getWeatherData(rawWeatherData)
        val airQualityDatas = getAirQualityData(rawAirQualityData)

        val timeSteps = arrayOf(1, 6)
        val forecasts = sortedMapOf<Night, DataAtLocationForNight>()

        // loops through time steps of use
        for (timeStep in timeSteps) {
            var previousTime: Datatime? = null

            // create new data lists
            var dataTimeSeries = mutableListOf<Datatime>()
            var tempTimeSeries = mutableListOf<Int>()
            var cloudsTimeSeries = mutableListOf<Int>()
            var windSpeedTimeSeries = mutableListOf<Int>()
            var airQualityTimeSeries = mutableListOf<Float>()

            for ((datatime, weatherData) in weatherDatas) {

                // break if forecasts for the next 5 days are have been collected
                if (forecasts.size >= 5) break

                // continue if the hoursIntoDay does not fit the interval time step.
                if (getHoursIntoDay(datatime) % timeStep != 0) continue

                if (previousTime == null || getNightNumber(datatime) == getNightNumber(previousTime)) {
                    // add values to the data lists
                    dataTimeSeries.add(datatime)
                    tempTimeSeries.add(weatherData.temperature)
                    cloudsTimeSeries.add(weatherData.clouds)
                    windSpeedTimeSeries.add(weatherData.windSpeed)
                    airQualityDatas[datatime]?.let { airQualityTimeSeries.add(it.aqi) }

                    previousTime = datatime
                    continue
                }

                // Break if the timeStep does not align with the expectancy. Means it most likely is
                // going from 1-hour intervals to 6-hour intervals. Breaking causes the loop to
                // shifts from 1-hour intervals to 6-hour intervals instead.
                if (getHoursBetween(datatime, previousTime) != timeStep) break

                // add values to the data lists
                dataTimeSeries.add(datatime)
                tempTimeSeries.add(weatherData.temperature)
                cloudsTimeSeries.add(weatherData.clouds)
                windSpeedTimeSeries.add(weatherData.windSpeed)
                airQualityDatas[datatime]?.let { airQualityTimeSeries.add(it.aqi) }

                val date = getDate(datatime)
                val sunData = getRawLocalData(API.SunriseSun, cords, date)

                val nightConditions = getNightConditionsSummary(
                    dataTimeSeries,
                    tempTimeSeries,
                    cloudsTimeSeries,
                    windSpeedTimeSeries,
                    airQualityTimeSeries,
                )

                // create forecast
                val forecast = DataAtLocationForNight(
                    night = plusDaysOfDatetime(previousTime, -1),
                    solarProperties = getSolarProperties(sunData),
                    conditions = nightConditions,
                )

                val night = Night(previousTime)

                // add the forecast if not already added for a smaller timeStep
                if (!forecasts.containsKey(night)) forecasts[night] = forecast

                // create new data lists
                dataTimeSeries = mutableListOf(datatime)
                tempTimeSeries = mutableListOf(weatherData.temperature)
                cloudsTimeSeries = mutableListOf(weatherData.clouds)
                windSpeedTimeSeries = mutableListOf(weatherData.windSpeed)
                airQualityTimeSeries = mutableListOf()
                airQualityDatas[datatime]?.let { airQualityTimeSeries.add(it.aqi) }

                previousTime = datatime
            }
        }

        return ForecastAtLocation(
            LocationDescription(cords, location),
            forecasts,
        )
    }

    /**
     * @return temperature, cloud area fraction and wind speed  expressing the upcoming weather from
     * [rawWeatherData]. Only the weather data of now- and upcoming timeframes, and of time step 1
     * hour, 6 hours and 12 hours, are kept.
     */
    private fun getWeatherData(rawWeatherData: JSONObject): SortedMap<Datatime, WeatherDataAtTime> {
        val weatherDatas: SortedMap<Datatime, WeatherDataAtTime> = sortedMapOf()

        val currentTime = getCurrentTime()
        val weatherTimeSeries =
            rawWeatherData.getJSONObject("properties").getJSONArray("timeseries")

        // loops through weather data of all time-series.
        for (i in 0 until weatherTimeSeries.length()) {
            val data = weatherTimeSeries[i] as JSONObject
            val datatime = parseToUTC(data.getString("time"))

            // remove data earlier than now-time
            if (datatime < currentTime) {
                continue
            }

            val values = data.getJSONObject("data")

            // exclude data points with a time step of 12 hours
            // exclude data points with missing time step value
            if (!values.has("next_1_hours") && !values.has("next_6_hours")) break

            val instantValues = values.getJSONObject("instant").getJSONObject("details")

            val airTemperature = instantValues.getString("air_temperature").toFloat().toInt()
            val clouds = instantValues.getString("cloud_area_fraction").toFloat().toInt()
            val windSpeed = instantValues.getString("wind_speed").toFloat().toInt()

            weatherDatas[datatime] = WeatherDataAtTime(airTemperature, clouds, windSpeed)
        }

        return weatherDatas
    }

    /**
     * @return the air quality index (AQI) for all given data points in [rawAirQualityData].
     */
    private fun getAirQualityData(rawAirQualityData: JSONObject?): HashMap<Datatime, AirQualityDataAtTime> {
        val airQualityData: HashMap<Datatime, AirQualityDataAtTime> = hashMapOf()

        if (rawAirQualityData == null) return airQualityData

        val message = rawAirQualityData.optString("message", "null")
        if (message.length > 15) {
            if (message.subSequence(0, 16) == "unknown location") return airQualityData
        }

        val airQualityTimeSeries = rawAirQualityData.getJSONObject("data").getJSONArray("time")
        for (i in 0 until airQualityTimeSeries.length() - 1) { // excludes last elements due to i not being in the same timeseries

            val data = airQualityTimeSeries[i] as JSONObject
            val datatime = parseToUTC(data.getString("from"))
            val values = data.getJSONObject("variables")

            val aqi = values.getJSONObject("AQI").getString("value").toFloat()

            airQualityData[datatime] = AirQualityDataAtTime(aqi)
        }

        return airQualityData
    }

    /**
     * @return sunset time as [Datatime] from [rawSunriseSunData]. Returns null if no sunset time exists.
     */
    private fun getSunsetTime(rawSunriseSunData: JSONObject?): Datatime? {
        if (rawSunriseSunData == null) return null

        val values = rawSunriseSunData.getJSONObject("properties")
        val sunsetTimeAsString: String? = values.getJSONObject("sunset").optString("time", "null")

        return (
            if (sunsetTimeAsString != "null") parseToUTC(sunsetTimeAsString!!)
            else null
        )
    }

    /**
     * @return sunrise time as [Datatime] from [rawSunriseSunData]. Returns null if no sunrise time exists.
     */
    fun getSolarProperties(rawSunriseSunData: JSONObject?): SolarProperties {
        if (rawSunriseSunData == null) return SolarProperties(null, null, 0.0)

        val values = rawSunriseSunData.getJSONObject("properties")

        val sunsetTimeAsString = values.getJSONObject("sunset").optString("time", "null")
        val sunriseTimeAsString = values.getJSONObject("sunrise").optString("time", "null")
        val solarMidnightElevation = values.getJSONObject("solarmidnight").getString("disc_centre_elevation").toDouble()

        val sunset = (
            if (sunsetTimeAsString != "null") parseToUTC(sunsetTimeAsString)
            else null
        )
        val sunrise = (
            if (sunriseTimeAsString != "null") parseToUTC(sunriseTimeAsString)
            else null
        )
        return SolarProperties(sunset,sunrise,solarMidnightElevation)
    }

    /**
     * @return [NightConditionSummary] object from the provided time-, temperature-, cloud-, wind
     * speed- and air quality data.
     */
    private fun getNightConditionsSummary(
        timeTimeSeries: List<Datatime>,
        tempTimeSeries: List<Int>,
        cloudsTimeSeries: List<Int>,
        windSpeedTimeSeries: List<Int>,
        airQualityTimeSeries: List<Float>,
    ): NightConditionsSummary {
        return NightConditionsSummary(
            maxTemp = tempTimeSeries.maxOf { it },
            minTemp = tempTimeSeries.minOf { it },
            maxWind = maxOf(a = 8, b = windSpeedTimeSeries.maxOf { it }),
            minWind = windSpeedTimeSeries.minOf { it },
            maxClouds = cloudsTimeSeries.maxOf { it },
            minClouds = cloudsTimeSeries.minOf { it },
            airPollution = (
                if (airQualityTimeSeries.size != timeTimeSeries.size) null
                else airQualityTimeSeries.average()
            ),
            times = timeTimeSeries,
            tempData = tempTimeSeries,
            cloudData = cloudsTimeSeries,
            windData = windSpeedTimeSeries
        )
    }
}
