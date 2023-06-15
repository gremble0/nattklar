package com.example.nattklar.view.globescreen.weatherwindow

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.nattklar.viewmodel.GlobeViewModel

/**
 * Shows a window containing useful information about the weather of the upcoming nights of a location.
 */
@Composable
fun LocationWeatherWindow(globeViewModel: GlobeViewModel) {
    val globeUiState by globeViewModel.uiState.collectAsState()

    if (globeUiState.loadingData) {
        LoadingWeatherDataDisplay()
    } else {
        // define anonymous function resetting the graphInfoBoxIndex
        val resetGraphBoxIndex = { globeViewModel.setGraphInfoBoxIndex(null) }
        val updatedResetGraphBoxIndex = rememberUpdatedState(resetGraphBoxIndex)

        Column (
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        // register when any object inside the column is tapped, which then calls for graphBoxInfoIndex reset
                        onTap = { updatedResetGraphBoxIndex.value.invoke() }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LocationDisplay(globeViewModel)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                UpcomingNightsButtons(globeViewModel)
                Spacer(modifier = Modifier.height(15.dp))
                SummarizedViewingConditionsText(globeViewModel)
            }

            // check if night data is available and sufficient
            NightConditionsDisplay(globeViewModel)
        }
    }
}
