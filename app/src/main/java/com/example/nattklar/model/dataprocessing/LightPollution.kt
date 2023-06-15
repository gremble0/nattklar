package com.example.nattklar.model.dataprocessing

import android.content.Context
import com.example.nattklar.model.DataNavigator
import com.example.nattklar.model.JsonAsset
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


/**
 * Responsible for loading and interpreting light pollution data previously processed and collected
 * into a json file. It is retrieved from a file because no provided API for this was found.
 */
object LightPollution {
    private val json = Json { ignoreUnknownKeys = true }

    // Light pollution data in a compressed and specific format
    private var lightPollutionData : List<List<List<Int>>>? = null

    /**
     * Loads 3360x2140 light pollution indexes for coordinates in scandinavia from json file in
     * assets. The values are representing the pixel values extracted from a high resolution image
     * containing light pollution data.
     */
    suspend fun loadLightPollutionData(context: Context) {
        // ensure data is not being loaded if it already has been
        if (lightPollutionData != null) return

        // load data
        val jsonString = DataNavigator.readJsonAsset(context, JsonAsset.LightPollution)
        lightPollutionData = json.decodeFromString(jsonString)
    }

    /**
     * @returns the interpolated light pollution index as an integer value between 0-11 (inclusive)
     * for the given [cords].
     */
    fun getIndexAtCoordinates(cords: LatLng): Int? {
        // return null if the data is missing
        if (lightPollutionData == null)
            return null

        // find absolute pixel coordinates from lat- and longitude based on a function found by
        // linear regression of analytic values of the light data picture
        val pixelX = 120.0981 * cords.longitude - 182.4666
        val pixelY = -120.0824 * cords.latitude + 8643.5663

        // collect closest integer pixel positions
        val closestPixels = listOf(
            Pair(pixelX.toInt(), pixelY.toInt()),
            Pair(pixelX.toInt() + 1, pixelY.toInt()),
            Pair(pixelX.toInt(), pixelY.toInt() + 1),
            Pair(pixelX.toInt() + 1, pixelY.toInt() + 1),
        )

        // calculate and return the weighted mean value of the light index of the surrounding pixels
        var avg = 0.0
        var totalDistance = 0.0
        for ((x, y) in closestPixels) {
            val distanceToLocation = euclideanDistance(x.toDouble(), y.toDouble(), pixelX, pixelY)

            val lightIndex = getLightPollutionFromPixelValues(x, y) ?: continue

            avg += distanceToLocation * lightIndex
            totalDistance += distanceToLocation
        }

        // return null if no indexes were found
        if (totalDistance == 0.0) return null

        // otherwise return the weighted mean (rounded down to closest integer)
        return (avg / totalDistance).roundToInt()
    }

    /**
     * @returns the Euclidean Distance between two points ([x1],[y1]) and ([x2],[y2])
     */
    private fun euclideanDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((x1 - x2).pow(2.0) + (y1 - y2).pow(2.0))
    }

    /**
     * @returns the light pollution index as an integer value between 0-11 (inclusive) for
     * [pixelX] and [pixelY]. The pixel values are representing longitude and latitude coordinates
     * of the data contained in raw light pollution data.
     */
    private fun getLightPollutionFromPixelValues(pixelX: Int, pixelY: Int): Int? {

        // return 1 as default value for missing or out-of-bounds data
        if (pixelX > 3359 || pixelX < 0 || pixelY > 2159 || pixelY < 0) return null

        // find the bucket and the location within where the pixel coordinates lie
        val maxBucketSize = 840
        val bucketNr = pixelX / maxBucketSize
        val xPosition = pixelX % maxBucketSize
        val bucket = lightPollutionData!![pixelY][bucketNr]

        // define either bottom-to-middle or top-to-middle iteration based on where the index is located
        var xPixelsBefore = minOf(xPosition, maxBucketSize-xPosition-1)
        val index = (
            if (xPosition < maxBucketSize / 2) { i: Int -> 2 * i }
            else { i: Int -> bucket.size - 2 * i - 2 }
        )

        // iterate through the bucket
        for (k in bucket.indices) {
            val lightIndex = bucket[index(k)]
            val occurrences = bucket[index(k)+1]

            if (xPixelsBefore > occurrences && k == maxBucketSize / 2) break
            if (xPixelsBefore <= occurrences) return lightIndex

            xPixelsBefore -= occurrences
        }
        return null
    }
}
