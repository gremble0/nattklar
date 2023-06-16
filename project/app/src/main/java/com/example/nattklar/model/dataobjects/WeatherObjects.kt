package com.example.nattklar.model.dataobjects

import com.example.nattklar.model.getHoursBetween
import com.example.nattklar.model.getNightNumber
import com.google.android.gms.maps.model.LatLng
import java.time.ZonedDateTime
import java.util.SortedMap

/**
 * This data class is used to classify nights. Nights are defined as the period from 12:00 UTC to
 * 11.59 UTC the next day.
 */
data class Night(val startDate: Datatime): Comparable<Night> {
    override fun compareTo(other: Night): Int {
        return getNightNumber(startDate) - getNightNumber(other.startDate)
    }
}

/**
 * This data class is used to evaluate identical times, but on different formats (zulu-time and
 * timezone times), as the same. The [ZonedDateTime] class did not provide this feature.
 */
data class Datatime(val time: ZonedDateTime): Comparable<Datatime> {
    override fun compareTo(other: Datatime): Int = getHoursBetween(this, other)
    override fun toString(): String = time.toString()
}

/**
 * This data class is used to describe information about a certain location. It is used to parse
 * data returned by the Google Geocoding and Google Places APIs to a format usable by other functions.
 */
data class LocationDescription(
    val cords: LatLng,
    val location: String,
)

/**
 * This data class is used to describe the [forecasts] for the upcoming nights for a given location
 * described by the [locationDescription].
 */
data class ForecastAtLocation(
    val locationDescription: LocationDescription,
    val forecasts: SortedMap<Night, DataAtLocationForNight>,
)

/**
 * This data class contains data at a given location for a specific [night]
 */
data class DataAtLocationForNight(
    val night: Datatime,
    val solarProperties: SolarProperties,
    val conditions: NightConditionsSummary,
)

/**
 * This class is used to store the night conditions for a night. This is used to store values in
 * the data class [DataAtLocationForNight]. The data points in [times] correspond to the values in
 * [tempData], [cloudData] and [windData].
 */
data class NightConditionsSummary(
    val minTemp: Int,
    val maxTemp: Int,
    val minWind: Int,
    val maxWind: Int,
    val minClouds: Int,
    val maxClouds: Int,
    val airPollution: Double?,
    val times: List<Datatime>,
    val tempData: List<Int>,
    val cloudData: List<Int>,
    val windData: List<Int>,
)

/**
 * This data class contains data for some solar properties of a given location.
 */
data class SolarProperties(
    val sunset: Datatime?,
    val sunrise: Datatime?,
    val solarMidnightElevation: Double,
)

/**
 * This data class contains useful measurement from the location forecast API. The class is used to
 * temporarily store the data before it is relocated to [NightConditionsSummary].
 */
data class WeatherDataAtTime(
    val temperature: Int,
    val clouds: Int,
    val windSpeed: Int,
)

/**
 * This data class contains useful measurement from the AirQualityForecast API. The class is used to
 * temporarily store the data before it is relocated to [NightConditionsSummary].
 */
data class AirQualityDataAtTime(
    val aqi: Float, // Air Quality Index
)

