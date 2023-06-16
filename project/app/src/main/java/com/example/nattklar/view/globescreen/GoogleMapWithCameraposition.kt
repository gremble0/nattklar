package com.example.nattklar.view.globescreen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.nattklar.viewmodel.GlobeViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.MapProperties
import kotlinx.coroutines.launch

/**
 * This composable shows a [GoogleMap] with information taken from the [globeViewModel]. It also
 * forwards onMapClick events by forwarding the event details to the [globeViewModel] which then
 * call methods inside model/. It also shows a [Marker] at the position the user has searched for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapWithCameraPosition(globeViewModel: GlobeViewModel, hasOpened: Boolean) {
    val globeScreenUiState by globeViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    GoogleMap(
        cameraPositionState = rememberCameraPositionState { position = globeScreenUiState.cameraPosition},
        properties = MapProperties(
            latLngBoundsForCameraTarget = LatLngBounds(
                LatLng(57.9, 4.6), LatLng(70.8,32.0)
            ),
            minZoomPreference = 5f,
            // Setting this variable accordingly based on user permissions.
            isMyLocationEnabled = (ActivityCompat.checkSelfPermission(
                LocalContext.current,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                LocalContext.current,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ),
        onMapClick = {
            coroutineScope.launch {
                globeViewModel.loadDataFromMapClick(it)
            }
        },
        // Needs to be defined as well because if it isn't nothing happens when user clicks on their location
        onMyLocationClick = {
            coroutineScope.launch {
                globeViewModel.loadDataFromMapClick(LatLng(it.latitude, it.longitude))
            }
        }
    ) {
        if (globeScreenUiState.sheetState.isVisible && hasOpened) {
            Marker(
                MarkerState(
                    position = LatLng(
                        globeScreenUiState.cameraPosition.target.latitude,
                        globeScreenUiState.cameraPosition.target.longitude
                    )
                )
            )
        }
    }
}
