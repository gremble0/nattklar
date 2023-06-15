package com.example.nattklar.model.dataprocessing

import android.content.Context
import com.example.nattklar.model.API
import com.example.nattklar.model.DataNavigator
import com.example.nattklar.model.TextAsset
import com.example.nattklar.model.dataobjects.Datatime
import com.example.nattklar.model.dataobjects.NightEvent
import com.example.nattklar.model.getCurrentTime
import com.example.nattklar.model.getDateAndTime
import com.example.nattklar.model.getDatetimeWithChangedHour
import com.example.nattklar.model.getHoursBetween
import com.example.nattklar.model.getHoursIntoDay
import com.example.nattklar.model.getNightNumber
import com.example.nattklar.model.parseToUTC
import com.example.nattklar.model.plusDaysOfDatetime
import kotlin.random.Random.Default.nextFloat

/**
 * Responsible for collecting and continuously updating interesting night and astronomical events.
 */
object NightEvents {
    private var nightEvents: List<NightEvent> = emptyList()
    private var lastTimeLoaded: Datatime? = null

    /**
     * Loads interesting astronomical night events including events regarding high polar light
     * activity.
     */
    suspend fun loadAndGetNightEvents(context: Context): List<NightEvent> {
        // ensure data is not being loaded if it previously has been
        if (lastTimeLoaded != null) {
            if (getHoursBetween(getCurrentTime(), lastTimeLoaded!!) < 6) return nightEvents
        }

        lastTimeLoaded = getCurrentTime()

        // load predefined and some earlier saved night events
        val storedEvents = loadStoredEvents(context)

        // check for high polar light activities
        val polarLightEvents = getPolarLightEvents()

        // filter out duplicated events
        nightEvents = extractUsefulNightEventsOf(storedEvents, polarLightEvents)

        // update text file
        DataNavigator.writeAndSaveTextAsset(
            context, nightEventsToString(), TextAsset.NightEvents
        )

        return nightEvents
    }

    fun getNightEvents() : List<NightEvent> {
        return nightEvents
    }

    /**
     * @return [String] representation of the loaded night events.
     */
    private fun nightEventsToString(): String {
        return nightEvents.joinToString(separator = "\n") {
            "${it.whenShown}|${it.eventType}|${it.title}|${it.shortDescription}|${it.description}"
        }
    }

    /**
     * Loads astronomical events from pre-written text file in assets.
     */
    private fun loadStoredEvents(context: Context): List<NightEvent> {
        val text = DataNavigator.readTextAsset(context, TextAsset.NightEvents)
        return (text?.split("\n")
            ?.filter { it.split("|").size == 5 }
            ?.map {
                val items = it.split("|")
                NightEvent(items[0], items[1], items[2], items[3], items[4])
            } ?: listOf()
        )
    }

    /**
     * @return KP-values, an index of the polar light activity, in 3-hour intervals the upcoming 2
     * days.
     */
    private suspend fun getPolarLightData(): List<HashMap<String, Float>>? {
        val rawData: String = DataNavigator.getRawDataFromAPI(API.PolarLight) ?: return null

        val lines = rawData.split("\n")

        // prepare a list of hashmaps to store polar light activities in
        val times: List<HashMap<String, Float>> = listOf(
            hashMapOf(),
            hashMapOf(),
            hashMapOf(),
        )

        var i = -1
        while (i < lines.size - 1) {
            i++

            // skip all the non-relevant data before the KP-values
            if (lines[i].length < 7 || lines[i].subSequence(0, 7) != "00-03UT") continue

            // read the 8 next lines containing the KP-values
            for (timePoint in 0..7) {
                val values = lines[i + timePoint]
                    .split(" ")
                    .filter { element -> element != ""}
                    .filter { element -> element[0] != '('}

                val timePeriod = values[0]

                times[0][timePeriod] = values[1].toFloat()
                times[1][timePeriod] = values[2].toFloat()
                times[2][timePeriod] = values[3].toFloat()
            }
            // skip all data after the KP-values
            break
        }

        return times
    }

    /**
     * @return eventual high polar light activities as a list of [NightEvent].
     */
    private suspend fun getPolarLightEvents(): List<NightEvent> {
        val polarLightData = getPolarLightData()

        val nightEvents: MutableList<NightEvent> = mutableListOf()

        if (polarLightData == null) return nightEvents

        // initialize values for iteration
        val currentHourOfDay = getHoursIntoDay(getCurrentTime())
        var whenHighActivity: Int? = null
        var highestActivityOfNight = 0f

        for (dayNumber in 0..2) {
            val nightActivity = polarLightData[dayNumber]

            nightActivity.onEachIndexed { i, (timePeriod, kpValue) ->
                val startHourOfPeriod = timePeriod.subSequence(3, 5).toString().toInt()

                // disregard data less than the now-time (or in which the time period already has began)
                if (dayNumber == 0) {
                    if (startHourOfPeriod < currentHourOfDay + 3) return@onEachIndexed
                }

                val threshold = 3

                // update whenHighActivity if this activity is 1) larger than the threshold, and 2) higher than the previously highest one.
                if (kpValue>threshold && kpValue>highestActivityOfNight) {
                    highestActivityOfNight = kpValue
                    whenHighActivity = startHourOfPeriod
                }

                // add the polar light activity as event to the night events list if a
                if (startHourOfPeriod % 24 == 12 || i == nightActivity.size-1) {
                    whenHighActivity?.let {
                        nightEvents.add(
                            NightEvent(
                                title = "Høy nordlysaktivitet",
                                eventType = "polar light",
                                whenShown = getDateAndTime(modifiedDatetime(dayNumber), asZulu = true),
                                shortDescription = "Det er høy nordlysaktivitet i vente!",
                                description = "I natt vil det være høy nordlysaktivitet! Ta turen ut og få et spektakulært syn!",
                            )
                        )
                        whenHighActivity = null
                        highestActivityOfNight = 0f
                    }
                }
            }
        }

        return nightEvents
    }

    /**
     * @return [Datatime] of the current datetime plus [dayNumber] days. The clock hour is remained
     * the same if >18 and <12, else the time is a random integer between 18 and 12.
     */
    private fun modifiedDatetime(dayNumber: Int): Datatime {
        val currentTime = getCurrentTime()
        val hoursIntoDay = getHoursIntoDay(currentTime)

        val hour: Int = if (dayNumber == 0 &&(hoursIntoDay >= 18 || hoursIntoDay < 12)) {
            hoursIntoDay
        } else {
            (nextFloat() * (18 - hoursIntoDay).toFloat() + hoursIntoDay).toInt()
        }

        return plusDaysOfDatetime(
            getDatetimeWithChangedHour(currentTime, hour), dayNumber)
    }

    /**
     * @return the combined interesting nights events for [storedEvents] and [polarLightEvents].
     * Since polar light events may be included in both, the events are checked for to not be a
     * duplicate before collected.
     */
    private fun extractUsefulNightEventsOf(
        storedEvents: List<NightEvent>,
        polarLightEvents: List<NightEvent>,
    ): List<NightEvent> {
        val usefulNightEvents: MutableList<NightEvent> = mutableListOf()

        // combined iteration of two sorted list to remove duplicated events in O(n+m) time.
        var i = 0
        var j = 0
        while (i < storedEvents.size && j < polarLightEvents.size) {
            val storedEventTime = getNightNumber(parseToUTC(storedEvents[i].whenShown))
            val polarLightEventTime = getNightNumber(parseToUTC(polarLightEvents[j].whenShown))

            // handle for equal dates
            if (storedEventTime == polarLightEventTime) {

                // add if stored event if not both of them are polar light
                if (storedEvents[i].eventType != "polar light" ||
                    polarLightEvents[i].eventType != "polar light"
                ) {
                    usefulNightEvents.add(polarLightEvents[j])
                }
                j++
            // handle unequal dates
            } else {
                if (storedEventTime > polarLightEventTime) {
                    usefulNightEvents.add(polarLightEvents[j])
                    j++
                } else {
                    usefulNightEvents.add(storedEvents[i])
                    i++
                }
            }
        }

        // add all remaining items since none of them are duplicates
        while (i < storedEvents.size) {
            usefulNightEvents.add(storedEvents[i])
            i++
        }
        while (j < polarLightEvents.size) {
            usefulNightEvents.add(polarLightEvents[j])
            j++
        }

        return usefulNightEvents
    }
}