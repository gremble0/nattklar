package com.example.nattklar.view.globescreen

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nattklar.view.globescreen.weatherwindow.LocationWeatherWindow
import com.example.nattklar.viewmodel.GlobeViewModel
import kotlinx.coroutines.launch

/**
 * This composable consists of a [ModalBottomSheet] containing the typical navigation buttons at the
 * top. The main contents of this composable are inside the [LocationWeatherWindow] composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetWeatherData(globeViewModel: GlobeViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val globeUiState by globeViewModel.uiState.collectAsState()

    ModalBottomSheet(
        sheetState = globeUiState.sheetState,
        onDismissRequest = {
            coroutineScope.launch {
                globeUiState.sheetState.hide()
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .defaultMinSize(minHeight = 150.dp)
    ) {
        LocationWeatherWindow(globeViewModel)
    }
}