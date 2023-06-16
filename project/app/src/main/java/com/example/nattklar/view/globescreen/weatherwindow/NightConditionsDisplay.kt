package com.example.nattklar.view.globescreen.weatherwindow

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nattklar.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.nattklar.model.dataobjects.NightConditionsSummary
import com.example.nattklar.model.getDayOfWeek
import com.example.nattklar.model.getNorwegianTime
import com.example.nattklar.viewmodel.GlobeViewModel
import java.util.Locale

/**
 * Shows a display containing specific information about the weather of a location and night.
 */
@Composable
fun NightConditionsDisplay(globeViewModel: GlobeViewModel) {
    val globeUiState by globeViewModel.uiState.collectAsState()

    if (globeUiState.nightDatas == null) return
    if (globeUiState.nightDatas!![globeUiState.nightNr!!] == null) return

    val nightNr = globeUiState.nightNr!!
    val nightData = globeUiState.nightDatas!![nightNr]!!

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiary)
            .fillMaxWidth(0.95f)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        WeatherTitle(
            modifier = Modifier,
            thisNight = (
                if (nightNr==0) "i"
                else getDayOfWeek(
                    getNorwegianTime(nightData.night)
                )
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        SummarizedWeatherConditions(nightData.conditions, globeUiState.lightPollutionIndex)
        GraphOfWeatherConditions(globeViewModel, nightData.conditions)
        Spacer(modifier = Modifier.height(35.dp))

    }
}

/**
 * Shows a text summarizing the stellar viewing conditions of the night.
 */
@Composable
fun SummarizedViewingConditionsText(globeViewModel: GlobeViewModel) {
    Text(
        text = globeViewModel.checkNightSkyViewingConditions(),
        style = TextStyle(color = MaterialTheme.colorScheme.primary),
        textAlign = TextAlign.Center,
        fontStyle = FontStyle.Italic,
        fontSize = 16.sp,
        modifier = Modifier
            .clip(shape = RoundedCornerShape(3.dp))
            //.background(MaterialTheme.colorScheme.secondary)
            .fillMaxWidth()
            .padding(10.dp),
    )
}

/**
 * Shows the title of the weather on the format "Været i natt" or "Været <night of the day>",
 * with an icon next to it.
 */
@Composable
fun WeatherTitle(
    modifier: Modifier,
    thisNight: String,
) {

    Row (
        verticalAlignment = Alignment.CenterVertically,
    ){
        Image(
            modifier = modifier
                .size(50.dp)
                .background(color = MaterialTheme.colorScheme.tertiary),
            painter = painterResource(id = R.drawable.klokke_hvit),
            contentDescription = "clock icon",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = "Været ${thisNight.replaceFirstChar { it.lowercase(Locale.getDefault()) }} natt",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp
        )
    }

}

/**
 * Shows the min-max interval of temperature, and a qualitative description of light pollution
 * and air quality.
 */
@Composable
fun SummarizedWeatherConditions(weather: NightConditionsSummary, lightPollutionIndex: Int?) {
    Row (
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // temperature
        Spacer(modifier = Modifier.width(5.dp))

        Image(
            modifier = Modifier
                .weight(1f, fill = false)
                .size(23.dp)
                .background(color = MaterialTheme.colorScheme.tertiary),
            painter = painterResource(id = R.drawable.temperatur_hvit),
            contentDescription = "temperature icon",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = "${weather.minTemp}-${weather.maxTemp}°",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.width(10.dp))

        // show the light pollution description for the location
        lightPollutionIndex?.let {
            val lightPollution = (
                if (it < 3) "lav"
                else if (it < 5) "lav"
                else if (it < 8) "middels"
                else if (it < 10) "høy"
                else "veldig høy"
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Lysstøy: $lightPollution",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.width(10.dp))
        }

        // show the air pollution description for the location
        weather.airPollution?.let {
            val airPollution = (
                if (it < 2) "lav"
                else if (it < 3) "middels"
                else if (it < 4) "høy"
                else "veldig høy"
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Luftkvalitet: $airPollution",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        }
    }
}
