package com.example.myapplication

import com.example.myapplication.model.round
import org.junit.Assert.assertTrue
import org.junit.Test

class DoubleRoundUnitTest {

    @Test
    fun DoubleRound_isCorrect() {
        // Arrange
        val values = listOf(3.1415, 2.5, 10.98765432, 99999999.5452)
        val decimals = listOf(2, 0, 4, 2)

        val expected = listOf(3.14, 3.0, 10.9877, 99999999.55)

        // Act
        val result = mutableListOf<Double>()
        values.zip(decimals).forEach {
            result.add(it.first.round(it.second))
        }

        // Assert
        val tol = 10E-8
        result.zip(expected).forEach {
            println(it)
            assertTrue(kotlin.math.abs(it.first-it.second) < tol)
        }
    }
}