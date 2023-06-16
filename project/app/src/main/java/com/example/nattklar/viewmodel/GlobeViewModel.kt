package com.example.nattklar.viewmodel

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nattklar.R
import com.example.nattklar.model.dataobjects.DataAtLocationForNight
import com.example.nattklar.model.dataobjects.ForecastAtLocation
import com.example.nattklar.model.dataprocessing.GoogleMaps
import com.example.nattklar.model.dataprocessing.LightPollution
import com.example.nattklar.model.dataprocessing.LocalWeather
import com.example.nattklar.model.dataobjects.GlobeUiState
import com.example.nattklar.model.getCurrentTime
import com.example.nattklar.model.getHoursBetween
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Responsible for holding and managing the UI-related data to be displayed on the newsscreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
class GlobeViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(
        GlobeUiState(
            searchSnackbar = SnackbarHostState(),
            cameraPosition = CameraPosition.fromLatLngZoom(LatLng(64.0, 10.0), 5f),
            sheetState = SheetState(
                skipPartiallyExpanded = false,
                initialValue = SheetValue.Expanded
            ),
            loadingData = true,
            forecastAtLocation = null,
            nightDatas = null,
            nightNr = null,
            graphInfoBoxIndex = null,
            lightPollutionIndex = null
        )
    )
    val uiState: StateFlow<GlobeUiState> = _uiState.asStateFlow()

    private var jobsRunning: Int = 0
    private var currentSetLocationJob: Job? = null

    /**
     * load lightPollutionData from json-file
     */
    fun loadLightPollutionData(context: Context) {
        viewModelScope.launch {
            LightPollution.loadLightPollutionData(context)
        }
    }

    /**
     * @return a [String] value representing the star night viewing conditions from various data in
     * the viewmodel.
     */
    fun checkNightSkyViewingConditions(): String {
        val nightDatas = uiState.value.nightDatas ?: return "-no data-"
        val nightNr = uiState.value.nightNr ?: return "-no data-"

        val locationData = nightDatas[nightNr]!!

        val conditions = locationData.conditions
        val solarProperties = locationData.solarProperties

        // collect useful values for evaluating the viewing conditions
        val lightPollution = uiState.value.lightPollutionIndex
        val sunrise = solarProperties.sunrise
        val sunset = solarProperties.sunset
        val solarMidnight = solarProperties.solarMidnightElevation
        val clouds = conditions.cloudData.average()
        val wind = conditions.windData.average()
        val airPollution = conditions.airPollution

        // initialise lists to store the bad and fine conditions
        val badConditions : MutableList<String> = mutableListOf()
        val fineConditions : MutableList<String> = mutableListOf()

        // check if the city light pollution is high at the location
        lightPollution?.let {
            if (lightPollution > 7) fineConditions.add("mye lysstøy")
        }

        // check if the night is worryingly short
        if (sunrise != null && sunset != null) {
            val nightLength = sunset.compareTo(sunrise)

            if (nightLength < 2) badConditions.add("en veldig kort natt")
            if (nightLength < 4) fineConditions.add("en litt kort natt")
        }
        else {
            if (solarMidnight > 0.0) badConditions.add("ingen natt")
        }

        // check if clouds, wind or air pollution may hinder the experience
        if (clouds > 50) badConditions.add("mye skyer")
        if (clouds > 30) fineConditions.add("noen skyer")
        if (wind > 6) badConditions.add("mye vind")
        if (wind > 4) fineConditions.add("en del vind")
        airPollution?.let {
            if (airPollution > 4.2) badConditions.add("veldig dårlig luftkvalitet")
            if (airPollution > 3.5) fineConditions.add("dårlig luftkvalitet")
        }

        // add note to the description of viewability
        var note : String? = null
        if (sunrise != null) {
            val currentTime = getCurrentTime()
            val nightHoursLeft = getHoursBetween(sunrise, currentTime)

            if (nightHoursLeft < 4) note = "natten er snart over"
            if (nightHoursLeft < 0) note = "natten er over"
        }

        // format the lists to a readable message
        if (badConditions.isNotEmpty()) {
            return formatViewabilityMessage("Dårlige", badConditions, note)
        }
        if (fineConditions.isNotEmpty()) {
            return formatViewabilityMessage("Greie", fineConditions, note)
        }

            // no bad night sky viewing conditions found, so the conditions must be good
        return formatViewabilityMessage("Gode", emptyList(), null)
    }

    /**
     * @return a [String] converted from an one-[wordDescription], a list of viewing [factors], and
     * an eventuell [note]. The returned string-value is a readable message used directly in the
     * view.
     */
    private fun formatViewabilityMessage(
        wordDescription: String,
        factors: List<String>,
        note: String?,
    ): String {

        var string = "$wordDescription stjernetittingsforhold"

        string = when (wordDescription) {
            "Gode" -> (
                if (note != null) "$string! Men $note."
                else "$string!"
            )
            "Greie" -> (
                if (note != null) "$string. Men $note."
                else "$string."
            )
            "Dårlige" -> (
                if (note != null) "$string. Og $note."
                else "$string."
            )
            else -> "."
        }

        if (wordDescription == "Gode") return string

        string += "\n Det er "

        // add commas between factors where there are more than 2
        for (condition in factors.dropLast(2)) {
            string += "$condition, "
        }

        // add the two last factors with " and " in-between
        if (factors.size >= 2) string += "${factors[factors.size-2]} og"
        return "$string ${factors.last()}."
    }


    /**
     * Updates the uiState locationData to that of the provided [cords].
     */
    private fun setLocationData(location: String, cords: LatLng) {
        jobsRunning++

        // empty the previous state
        _uiState.update { currentState -> currentState.copy(
            loadingData = true,
            forecastAtLocation = null,
            nightDatas = null,
            nightNr = null,
            graphInfoBoxIndex = null,
            lightPollutionIndex = null
        )}

        // fill the set with new data
        currentSetLocationJob = viewModelScope.launch {
            val forecastAtLocation = LocalWeather.getLocalData(location, cords)
            jobsRunning--

            _uiState.update { currentState ->
                currentState.copy(
                    loadingData = jobsRunning != 0,
                    forecastAtLocation = forecastAtLocation,
                    nightDatas = getNightDatas(forecastAtLocation),
                    nightNr = getInitialNightNr(forecastAtLocation),
                    lightPollutionIndex = LightPollution.getIndexAtCoordinates(cords)
                )
            }
        }

        // update the current job to null
        currentSetLocationJob = null
    }

    /**
     * Updates the uiState nightNr to the provided [nightNr], or handles the possible errors it may result in.
     */
    fun setNightNr(nightNr: Int) {
        val locationData = uiState.value.forecastAtLocation
        if (locationData == null) {
            println("ALERT: The attempted update of uiState.value.nightNr was stopped as the locationData is missing.")
            return
        }
        val nightsKeys = locationData.forecasts.keys
        if (nightNr >= nightsKeys.size) {
            println("ALERT: The attempted update of uiState.value.nightNr to $nightNr was stopped as it bypasses the maximum given nights of the location.")
            return
        }

        // update the value
        _uiState.update { currentState ->
            currentState.copy(nightNr = nightNr)
        }
    }

    /**
     * Updates the uiState graphInfoBoxIndex to the provided [index].
     */
    fun setGraphInfoBoxIndex(index: Int?) {
        _uiState.update { currentState ->
            currentState.copy(graphInfoBoxIndex = index)
        }
    }

    /**
     * @return night data from a given [locationData], and handles missing data, if any.
     */
    private fun getNightDatas(locationData: ForecastAtLocation?): List<DataAtLocationForNight?>? {
        if (locationData == null) return null

        val nightDatas: MutableList<DataAtLocationForNight?> = mutableListOf()
        val forecasts = locationData.forecasts.values

        // collect data elements
        for (value in forecasts) nightDatas.add(value)
        // create null elements
        for (i in 0..(4-nightDatas.size)) nightDatas.add(null)

        return nightDatas
    }

    /**
     * @return the initial nightNr from a [locationData] as either 0 or null, depending on whether
     * the data in [locationData] is missing.
     */
    private fun getInitialNightNr(locationData: ForecastAtLocation?): Int? {
        if (locationData == null) return null
        return 0
    }

    /**
     * Loads data from searchbar based on the [selectedLocation] parameter and shows the BottomSheet
     */
    // TODO: fix formatting on ambiguous searches and stop from crashing on "asdasdas"-type searches
    suspend fun loadDataFromSearch(selectedLocation: String) {
        val placesLocationData = GoogleMaps.getCordsFromLocation(selectedLocation)
        val placesCords = placesLocationData?.cords ?: LatLng(0.0, 0.0)
        val placesLocation = placesLocationData?.location ?: ""

        // show error message informing the user of unreasonable values provided for a search.
        if (isReasonableSearch(placesLocation)) {
            // Show bottomsheet, initially displaying loading screen
            uiState.value.sheetState.show()

            formatAndSetLocation(
                placesLocation,
                placesCords
            )

            _uiState.update { currentState ->
                currentState.copy(cameraPosition = CameraPosition.fromLatLngZoom(placesCords, 10f))
            }
        } else {
            uiState.value.searchSnackbar.showSnackbar(
                message = getErrorDescription(placesLocation),
                duration = SnackbarDuration.Short,
                actionLabel = "Skjul"
            )
        }
    }

    /**
     * Loads data after mapClick event based on the [cords] parameter and shows the BottomSheet
     */
    suspend fun loadDataFromMapClick(cords: LatLng) {
        // If the sheetState is visible, return and don't load data
        if (uiState.value.sheetState.isVisible) {
            return
        }

        val geocodingLocation = GoogleMaps.getLocationFromCords(cords)
        if (isReasonableSearch(geocodingLocation)) {
            uiState.value.sheetState.show()
            _uiState.update { currentState->
                currentState.copy(cameraPosition = CameraPosition.fromLatLngZoom(cords, 10f))
            }

            formatAndSetLocation(
                geocodingLocation,
                cords
            )
        } else {
            // If the user clicks outside the map, display error on snackbar and don't load data
            uiState.value.searchSnackbar.showSnackbar(
                message = getErrorDescription(geocodingLocation),
                duration = SnackbarDuration.Short,
                actionLabel = "Skjul"
            )
        }
    }


    /**
     * Update the uiState with the formatted [location] and [cords].
     */
    fun formatAndSetLocation(
        location: String?,
        cords: LatLng
    ) {
        // Places formats formatted_address(location) as "Blindern, Oslo, Norway".
        // Geocoding formats long_name(location) with municipality in norwegian like "Oslo kommune".
        // We only want the city name in norwegian so we format it a little bit.
        val formattedLocation = location!!.replace(", Norway", "")

        setLocationData(formattedLocation, cords)
    }

    /**
     *  @return the icon display based on the forecast.
     */
    fun getSymbolForDay(index: Int): Int {
        val dayConditions = uiState.value.nightDatas?.get(index)?.conditions

        val meanSkyCover = dayConditions?.cloudData?.average()
        val meanWind = dayConditions?.windData?.average()

        if (meanWind != null && meanSkyCover != null) {
            if (meanSkyCover >= 75) return R.drawable.vaer_skyet
            else if (meanWind >= 8) return R.drawable.vind_uten_pil
            else if (meanSkyCover >= 30) return R.drawable.vaer_skyet
        }
        return R.drawable.vaer_klart
    }

    /**
     * @return true if [location] contains the substring "Norway" and is not null
     */
    private fun isReasonableSearch(location: String?): Boolean {
        // From our implementations of the API parsing we know that the location is valid if it
        // contains the substring "Norway".
        return !(location == null || !location.contains("Norway"))
    }

    /**
     * @return a [String] containing an appropriate error message based on the [location] parameter.
     */
    private fun getErrorDescription(location: String?): String {
        if (location == null) return "Du trykket utenfor Norge"
        else if (location == "") return "Søkefeltet er tomt"
        else if (!location.contains("Norway")) return "Det oppgitte stedet ligger ikke i Norge"
        return ""
    }
}
