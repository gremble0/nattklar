package com.example.nattklar.model.dataprocessing

import com.example.nattklar.model.API
import com.example.nattklar.model.DataNavigator.getRawDataFromAPI
import com.example.nattklar.model.dataobjects.LocationDescription
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

/**
 * Object for handling user input and converting it into coordinates parsable by other APIs.
 * It makes outgoing API requests to the Google Geocoding and Google Places APIs
 */
object GoogleMaps {
    // Provide your own api key here, it also needs to be set inside AndroidManifest.xml
    private const val apiKey = ""
    /**
     * @return [LocationDescription] data class based on the response of the google places API
     * of querying with the [location] parameter. If [location] is an empty string return null.
     */
    suspend fun getCordsFromLocation(location: String): LocationDescription? {
        if (location == "") {
            return null
        }

        val body = getRawDataFromAPI(API.GooglePlaces, location, apiKey) ?: return null
        val obj = JSONObject(body)
        val resultsObj: JSONArray = obj["results"] as JSONArray

        if (resultsObj.length()==0) return null

        val placeObj: JSONObject = resultsObj[0] as JSONObject
        val position: String = placeObj["formatted_address"] as String
        val geometryObj: JSONObject = placeObj["geometry"] as JSONObject
        val locationObj: JSONObject = geometryObj["location"] as JSONObject

        val lat: Double = locationObj["lat"] as Double
        val lng: Double = locationObj["lng"] as Double

        return LocationDescription(
            LatLng(lat, lng),
            position,
        )
    }

    /**
     * @return [String] representing the location of the [cords] parameter based on the response
     * of the google geocoding API. It return null if ...
     */
    suspend fun getLocationFromCords(cords: LatLng): String? {
        val body = getRawDataFromAPI(
            API.Geocoding, cords.latitude.toString(), cords.longitude.toString(), apiKey
        ) ?: return null

        val obj = JSONObject(body)
        val resultsObj: JSONArray = obj["results"] as JSONArray

        var locationName = ""

        // loop through each result in API response
        for (i in 0 until resultsObj.length()) {
            val resultObj = resultsObj.getJSONObject(i)
            val addressComponents = resultObj.getJSONArray("address_components")

            // loop through each address_component in API response
            for (j in 0 until addressComponents.length()) {
                val addressComponent = addressComponents.getJSONObject(j)
                val types = addressComponent.getJSONArray("types")
                if (types[0] == "country" && addressComponent["long_name"] as String != "Norway") {
                    return null
                }
                // postal_town is the "addressComponent" that makes the most sense to fetch - equivalent to city/town
                if (types[0] == "postal_town" && locationName == "") {
                    locationName = addressComponent["long_name"] as String + ", Norway"
                }
            }
        }

        return locationName
    }
}