package com.example.myapplication

import com.example.nattklar.model.dataprocessing.GoogleMaps
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GoogleMapsUnitTest {

    @Test
    fun convert_cords_to_place_name_isCorrect() = runBlocking {
        // Arrange
        val coords = listOf (
            LatLng(59.912997, 10.737997),
            LatLng(63.429381066489164, 10.393410449366485),
            LatLng(58.97, 5.731389),
            LatLng(60.383333, 5.333333)
        )

        val expectedValues = listOf(
            "Oslo, Norway",
            "Trondheim, Norway",
            "Stavanger, Norway",
            "Bergen, Norway"
        )

        // Act
        val results = mutableListOf<String?>()
        coords.forEach {
            results.add(GoogleMaps.getLocationFromCords(it))
        }

        // Assert
        results.zip(expectedValues).forEach {
            assertEquals(it.first, it.second)
        }
    }

    @Test
    fun convert_place_name_to_cords_isCorrect() = runBlocking {
        // Arrange
        val places = listOf (
            "Kristiansand",
            "Tromsø",
            "Bodø",
            "Drammen"
        )

        val expectedValues = listOf(
            LatLng(58.158, 8.016),
            LatLng(69.650, 18.956),
            LatLng(67.282, 14.394),
            LatLng(59.740, 10.203)
        )

        // Act
        val results = mutableListOf<LatLng?>()
        places.forEach {
            results.add(GoogleMaps.getCordsFromLocation(it)?.cords)
        }

        // Assert
        val tolLat = 0.01  // Greie terskelverdier?
        val tolLng = 0.02

        results.zip(expectedValues).forEach {
            if (it.first != null) {
                assertTrue( kotlin.math.abs(it.first!!.latitude - it.second.latitude) < tolLat )
                assertTrue( kotlin.math.abs(it.first!!.longitude - it.second.longitude) < tolLng )
            }
        }

    }
}