package com.example.myapplication

import com.example.myapplication.viewmodel.GlobeViewModel
import org.junit.Assert.*
import org.junit.Test

class GlobeViewModelUnitTest {

    @Test
    fun isReasonableSearch_isCorrect() {
        // The lines below are to omit the private-encapsulation
        val methodName = "isReasonableSearch"
        val method = GlobeViewModel::class.java.getDeclaredMethod(methodName, String::class.java)
        method.isAccessible = true

        // Arrange
        val testStrings = listOf(
            "Oslo, Norway",
            "Norway, Oslo",
            "Norway",
            "Norge",
            "NorwayNorway",
            "",
            null
        )

        val expectedBooleans = listOf(
            true,
            true,
            true,
            false,
            true,
            false,
            false
        )

        // Act
        val result = mutableListOf<Boolean>()
        testStrings.forEach {
            result.add(method.invoke(GlobeViewModel(), it) as Boolean)
        }

        // Assert
        result.zip(expectedBooleans).forEach {
            assertEquals(it.first, it.second)
        }
    }
}