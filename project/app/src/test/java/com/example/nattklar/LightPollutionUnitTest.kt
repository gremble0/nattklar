package com.example.myapplication

import com.example.nattklar.model.dataprocessing.LightPollution

import org.junit.Assert.*
import org.junit.Test

class LightPollutionUnitTest {

    @Test
    fun euclideanDistance_isCorrect() {
        // The lines below are to omit the private-encapsulation
        val methodName = "euclideanDistance"
        val parameterTypes = arrayOf(
            Double::class.java,
            Double::class.java,
            Double::class.java,
            Double::class.java,
        )

        val method = LightPollution::class.java.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true

        // Arrange
        val points = listOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(1, 2, 3, 4),
            arrayOf(10000, -2000, 333, 1444),
            arrayOf(9999999999, 9999999999, -9999999999, 9999999999)
        )

        // used calculator: https://www.calculatorsoup.com/calculators/geometry-plane/distance-two-points.php
        val expectedValues = listOf(
            0.0,
            2.828427,
            10262.164733,
            19999999998.0
        )

        // Act
        val results = mutableListOf<Double>()
        points.forEach {
            results.add(method.invoke(LightPollution, *it) as Double)
        }

        // Assert
        val tol = 10E-6  // The calculator gave precision of 6 decimals
        results.zip(expectedValues).forEach {
            assertTrue( kotlin.math.abs(it.first-it.second) < tol )
        }
    }
}