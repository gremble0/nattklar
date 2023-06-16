package com.example.nattklar.model

import android.content.Context
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.MessageFormat

enum class JsonAsset { WikiText, LightPollution, Stars, Constellations }
enum class TextAsset { NightEvents }
enum class API { LocationForecast, AirQuality, SunriseSun, PolarLight, GooglePlaces, Geocoding }

/**
 * Responsible for handling in and out data from the app. Among it's responsibilities lies making
 * [API] requests, reading and parsing [JsonAsset]s, and reading and writing to [TextAsset].
 */
object DataNavigator {
    private val client = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    // These are the external APIs and assets used in the project
    private val APIs: HashMap<API, String> = hashMapOf(
        API.LocationForecast to "https://api.met.no/weatherapi/locationforecast/2.0/complete?lat={0}&lon={1}",
        API.AirQuality to "https://api.met.no/weatherapi/airqualityforecast/0.1/?lat={0}&lon={1}",
        API.SunriseSun to "https://api.met.no/weatherapi/sunrise/3.0/sun?lat={0}&lon={1}&date={2}",
        API.PolarLight to "https://services.swpc.noaa.gov/text/3-day-forecast.txt",
        API.GooglePlaces to "https://maps.googleapis.com/maps/api/place/textsearch/json?query={0}&key={1}",
        API.Geocoding to "https://maps.googleapis.com/maps/api/geocode/json?latlng={0},{1}&key={2}",
    )
    private val jsonAssets: HashMap<JsonAsset, String> = hashMapOf(
        JsonAsset.WikiText to "articles.json",
        JsonAsset.LightPollution to "light-pollution-data.json",
        JsonAsset.Stars to "stars.json",
        JsonAsset.Constellations to "constellations.json",
    )
    private val textAssets: HashMap<TextAsset, String> = hashMapOf(
        TextAsset.NightEvents to "night-events.txt",
    )

    /**
     *  @return the string representation of the data read from a provided [jsonAsset].
     */
    suspend fun readJsonAsset(context: Context, jsonAsset: JsonAsset): String {
        val file = jsonAssets[jsonAsset]!!
        Log.i("JSON callback to \"$file\"", "Success")

        return withContext(Dispatchers.IO) {
            context.assets.open(file).bufferedReader().use { it.readText() }
        }
    }

    /**
     * @return string representation of data read from a provided [textAsset].
     */
    fun readTextAsset(context: Context, textAsset: TextAsset): String? {
        val file = textAssets[textAsset]!!

        try {
            val input = context.assets.open(file)
            val size: Int = input.available()
            val buffer = ByteArray(size)
            input.read(buffer)
            input.close()
            Log.i("JSON callback to \"$file\"", "Success")

            return String(buffer)
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("Exception", "Can not read file: $file")
        }
        
        return null
    }

    /**
     *  Writes some [data] represented as a [String] to a provided [textAsset].
     */
    fun writeAndSaveTextAsset(context: Context, data: String, textAsset: TextAsset) {
        val file = textAssets[textAsset]!!

        // TODO: Asset-folder is read-only resulting in the code below to not work
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "Write and save of file failed: $file")
        }
    }

    /**
     * @return JSONObject with raw data from a HTTP-response provided by [API] with some [queries].
     */
    suspend fun getRawDataFromAPI(api: API, vararg queries: String): String? {
        val path = MessageFormat.format(APIs[api], * queries)
        val response = client.get(path)

        Log.i("API callback to \"$path\"", response.toString())
        
        return response.body()
    }
}