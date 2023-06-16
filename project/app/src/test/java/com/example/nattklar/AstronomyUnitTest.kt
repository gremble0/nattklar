package com.example.myapplication

import com.example.nattklar.model.dataobjects.Datatime
import com.example.nattklar.model.dataprocessing.Astronomy

import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime


class AstronomyUnitTest {
    private val testData = listOf(
        Datatime(ZonedDateTime.of(
            2023,
            5,
            26,
            23,
            59,
            59,
            0,
            ZoneId.of("Europe/Oslo")
        )),
        Datatime(ZonedDateTime.of(
            2000,
            5,
            12,
            12,
            30,
            30,
            0,
            ZoneId.of("Europe/Oslo")
        )),
        Datatime(ZonedDateTime.of(
            1313,
            3,
            30,
            13,
            26,
            13,
            0,
            ZoneId.of("Europe/Oslo")
        )),
        Datatime(ZonedDateTime.of(
            30023,
            8,
            8,
            8,
            8,
            8,
            0,
            ZoneId.of("Europe/Oslo")
        ))
    )

    @Test
    fun calculate_JulianDate_isCorrect() {
        // The lines below are to omit the private-encapsulation
        val methodName = "calculateJulianDate"
        val parameterTypes = arrayOf(Datatime::class.java)

        val method = Astronomy::class.java.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true

        // Arrange
        // used calculator: https://www.phpsciencelabs.com/sidereal-time-calculator/index.php
        // Greenwich Mean Sidereal Time have been used, UT+2
        val expectedValues = listOf(
            2460090.5,
            2451676.5,
            2200711.5,
            12686954.5
        )

        // Act
        val result = mutableListOf<Double>()
        testData.forEach {
            result.add(method.invoke(Astronomy, it) as Double)
        }

        // Assert
        val tol = 10E-8

        result.zip(expectedValues).forEach {
            assertTrue( kotlin.math.abs(it.first - it.second) < tol )
        }
    }

    @Test
    fun convert_to_GMST_isCorrect() {
        // The lines below are to omit the private-encapsulation
        val methodName = "convertToGMST"
        val parameterTypes = arrayOf(Datatime::class.java)

        val method = Astronomy::class.java.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true

        // Arrange
        // used calculator: https://www.phpsciencelabs.com/sidereal-time-calculator/index.php
        // Greenwich Mean Sidereal Time have been used, UT+2
        val expectedValues = listOf(
            14.2806360148,  // h,
            1.8753206664,
            23.9416092863,
            5.8041810036
        )

        // Act
        val result = mutableListOf<Double>()
        testData.forEach {
            result.add(method.invoke(Astronomy, it) as Double)
        }

        // Assert
        val tol = 10E-3  // Had to make it lower due to different results from different calculators

        result.zip(expectedValues).forEach {
            assertTrue( kotlin.math.abs(it.first - it.second) < tol )
        }
    }

    @Test
    fun convert_to_JulianTime_isCorrect() {
        // The lines below are to omit the private-encapsulation
        val methodName = "calculateJulianTime"
        val parameterTypes = arrayOf(Datatime::class.java)

        val method = Astronomy::class.java.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true

        // Arrange
        // used calculator: https://www.phpsciencelabs.com/sidereal-time-calculator/index.php
        // REMEMBER: UT+ 02:00 Local Time Zone Offset
        val expectedValues = listOf(
            2460091.416655093,
            2451676.937847222,
            2200711.976539352,
            12686954.75564815
        )

        // Act
        val result = mutableListOf<Double>()
        testData.forEach {
            result.add(method.invoke(Astronomy, it) as Double)
        }

        // Assert
        val tol = 10E-8

        result.zip(expectedValues).forEach {
            assertTrue( kotlin.math.abs(it.first - it.second) < tol )
        }
    }
}