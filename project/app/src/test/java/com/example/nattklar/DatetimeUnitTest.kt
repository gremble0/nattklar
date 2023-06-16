package com.example.myapplication

import com.example.nattklar.model.dataobjects.Datatime
import com.example.myapplication.model.getDayOfWeek

import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DatetimeUnitTest {

    @Test
    fun getDayOfWeek_isCorrect() {
        // Arrange
        val dates = listOf(
            Datatime(ZonedDateTime.of(
                2023,
                5,
                26,
                12,
                30,
                0,
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

        val expectedValues = listOf(
            "Fredag",
            "Fredag",
            "Fredag",
            "Søndag"
        )

        // Act
        val result = mutableListOf<String>()
        dates.forEach {
            result.add(getDayOfWeek(it))
        }

        // Assert
        result.zip(expectedValues).forEach {
            assertEquals(it.first, it.second)
        }
    }
}