package com.example.nattklar.model

import com.example.nattklar.model.dataobjects.Datatime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * @return a Datetime of the UTC-time [datetime].
 */
fun parseToUTC(datetime: String): Datatime {
    val zonedDateTime = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val time = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC").normalized())
    return Datatime(time)
}

/**
 * @return a [Datatime] of the current time .
 */
fun getCurrentTime(): Datatime {
    return Datatime(ZonedDateTime.now())
}

/**
 * @return the number of nights between the previous year and [Datatime]. A night is defined to be
 * from 12:00 UTC to 11:59 UTC the next day.
 */
fun getNightNumber(datatime: Datatime): Int {
    val days = getDayOfYear(datatime)
    val hours = getHoursIntoDay(datatime)
    return (24 * days + hours + 12) / 24
}

/**
 * @return [String] representing the date on the format uuuu-MM-dd of a provided [Datatime].
 */
fun getDate(datatime: Datatime, split : String = "-"): String {
    return datatime.time.format(
        DateTimeFormatter.ofPattern("uuuu${split}MM${split}dd")
    )
}

/**
 * @return the time of the day on the format HH:mm from the provided [Datatime].
 */
fun getTimeOfDay(datatime: Datatime): String {
    return datatime.time.format(
        DateTimeFormatter.ofPattern("HH:mm")
    )
}

/**
 * @return the week day as a full string of a provided [Datatime].
 */
fun getDayOfWeek(datatime: Datatime): String {
    val dayInEnglish = datatime.time.format(
        DateTimeFormatter.ofPattern("EE")
    )
    val englishToNorwegianDays = hashMapOf(
        "Mon" to "Mandag",
        "Tue" to "Tirsdag",
        "Wed" to "Onsdag",
        "Thu" to "Torsdag",
        "Fri" to "Fredag",
        "Sat" to "Lørdag",
        "Sun" to "Søndag",
    )
    return englishToNorwegianDays[dayInEnglish]!!
}

/**
 * @return the number of hours between [firstDatetime] and [secondDatetime].
 */
fun getHoursBetween(firstDatetime: Datatime, secondDatetime: Datatime): Int {
    return ChronoUnit.HOURS.between(secondDatetime.time,firstDatetime.time).toInt()
}

/**
 * @return the converted [Datatime] of the provided [Datatime] with respect to a given [zone].
 */
fun getZoneTime(datetime: Datatime, zone: String): Datatime {
    return Datatime(datetime.time.withZoneSameInstant(ZoneId.of(zone)))
}

/**
 * @return the norwegian time as [Datatime] from a provided [Datatime].
 */
fun getNorwegianTime(datetime: Datatime) : Datatime {
    return getZoneTime(datetime,"Europe/Oslo")
}

/**
 * @return the number of days since previous year for a provided [Datatime].
 */
fun getDayOfYear(datatime: Datatime): Int {
    return datatime.time.format(
        DateTimeFormatter.ofPattern("D")).toInt()
}

/**
 * @return the offset of a [Datatime] as a [String].
 */
fun getOffset(datatime: Datatime): String {
    return datatime.time.format(
        DateTimeFormatter.ofPattern("X"))
}

/**
 * @return a [String] on format "<date>T<timeOfDay><offset> of a provided [Datatime].
 */
fun getDateAndTime(datatime: Datatime, asZulu: Boolean = false): String {
    val convertedDatetime = (
        if (asZulu) getZoneTime(datatime,"Z")
        else datatime
    )
    return "${getDate(convertedDatetime)}T${getTimeOfDay(convertedDatetime)}${getOffset(convertedDatetime)}"
}

/**
 * @return the number of hours since the beginning of the day of a [Datatime].
 */
fun getHoursIntoDay(datatime: Datatime): Int {
    return datatime.time.hour % 24
}

/**
 * @return the clock hour of the day of a [Datatime].
 */
fun getClockHour(datatime: Datatime): String {
    return datatime.time.format(
        DateTimeFormatter.ofPattern("HH"))
}

/**
 * @return a new [Datatime] with the same time of a provided [Datatime] plus [numberOfDays] days.
 */
fun plusDaysOfDatetime(datatime: Datatime, numberOfDays: Int): Datatime {
    return Datatime(datatime.time.plusDays(numberOfDays.toLong()))
}

/**
 * @return a new [Datatime] with the same time of a provided [Datatime] with changed clock time to
 * [hour] hours and [minute] minutes. [minute] will be defaulted to 0 if no were provided.
 */
fun getDatetimeWithChangedHour(datatime: Datatime, hour: Int, minute: Int = 0): Datatime {
    return Datatime(datatime.time.with ( LocalTime.of(hour, minute)))
}