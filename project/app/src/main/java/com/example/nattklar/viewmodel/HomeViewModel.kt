package com.example.nattklar.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.nattklar.model.dataprocessing.*
import com.google.android.gms.maps.model.LatLng
import com.example.nattklar.model.dataobjects.Article
import com.example.nattklar.model.dataobjects.HomeUiState
import com.example.nattklar.model.getCurrentTime
import com.example.nattklar.model.getNorwegianTime
import com.example.nattklar.model.getTimeOfDay
import com.example.nattklar.model.dataprocessing.Articles
import com.example.nattklar.model.dataprocessing.Astronomy
import com.example.nattklar.model.dataprocessing.LocalWeather
import kotlinx.coroutines.flow.update

class HomeViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(listOf(), listOf(), null, true)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadArticles(context: Context) {
        var articles : List<Article>

        viewModelScope.launch {
            articles = Articles.loadArticles(context)

            _uiState.update { currentState ->
                currentState.copy(
                    constellationArticlesShuffled = articles.filter{ it.category == "Stjernebilder" }.shuffled(),
                    articlesLoading = false,
                )
            }
        }
    }

    /**
     *  Loads astronomic data from json files the they are stored in.
     */
    fun loadAstronomicData(context: Context) {
        viewModelScope.launch {
            Astronomy.loadAstronomicData(context)

        }
    }

    fun loadNightEvents(context: Context) {
        // load night events
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    news = NightEvents.loadAndGetNightEvents(context)
                )
            }
        }
    }

    fun loadSolarProperties(latLng: LatLng) {
        viewModelScope.launch {
            val rawSolarData = Astronomy.loadCoordinatesData(latLng)

            _uiState.update { currentState ->
                currentState.copy(
                    solarProperties = LocalWeather.getSolarProperties(rawSolarData)
                )
            }
        }
    }

    fun isSunset(): Boolean {
        val solarProperties = uiState.value.solarProperties ?: return true
        if (solarProperties.sunrise == null) return true
        if (solarProperties.sunset == null) return false

        val currentTime = getCurrentTime()

        return currentTime < solarProperties.sunset
    }

    fun getSolarDisplay(): String {
        val solarProperties = uiState.value.solarProperties ?: return ""

        if (solarProperties.sunset == null && solarProperties.sunrise == null) {
            // check for midnight sun and polar night
            if (solarProperties.solarMidnightElevation < 0.0) return "polarnatt"
            return "midnattssol"
        }

        val currentTime = getCurrentTime()

        // check if sunset but not sunrise
        if (solarProperties.sunrise == null) {
            if (currentTime < solarProperties.sunset!!) return "polarnatt"
            return getTimeOfDay(getNorwegianTime(solarProperties.sunset))
        }

        // check if sunrise but not sunset
        if (solarProperties.sunset == null) {
            if (currentTime < solarProperties.sunrise) return "midnattsol"
            return getTimeOfDay(getNorwegianTime(solarProperties.sunrise))
        }

        // both sunrise and sunset exist
        if (currentTime < solarProperties.sunset) return getTimeOfDay(getNorwegianTime(solarProperties.sunset))
        return getTimeOfDay(getNorwegianTime(solarProperties.sunrise))
    }

    /**
     *  Returns the qualitative description of the viewability of a constellation at some [coords].
     *  If no constellation have the provided [constellationName], a default value of "ingen" will
     *  be returned.
     */
    fun getConstellationVisibility(constellationName : String, coords : LatLng) : String {
        var percentageHours = 0.0
        viewModelScope.launch {
            percentageHours = Astronomy.visibilityOfConstellation(constellationName, coords)
        }

        if (percentageHours > 0.8) return "Utmerket"
        if (percentageHours > 0.6) return "God"
        if (percentageHours > 0.4) return "Noe"
        if (percentageHours > 0.2) return "Lite"
        return "Ingen"
    }
}