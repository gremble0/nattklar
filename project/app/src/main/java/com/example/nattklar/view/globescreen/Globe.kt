package com.example.nattklar.view.globescreen

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nattklar.model.dataprocessing.GoogleMaps.getLocationFromCords
import com.example.nattklar.view.BottomNavigationBar
import com.example.nattklar.viewmodel.GlobeViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

/**
 * Displays the globe screen with a map, searchbar and bottom window with weather location info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Globe(
    navController: NavController,
    globeViewModel: GlobeViewModel = viewModel(),
) {
    val context = LocalContext.current as Activity
    globeViewModel.loadLightPollutionData(context)

    val globeUiState by globeViewModel.uiState.collectAsState()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val coroutineScope = rememberCoroutineScope()

    val selected by remember { mutableStateOf("globe") }
    var hasOpened by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = globeUiState.searchSnackbar,
            ) {
                Snackbar(
                    actionColor = MaterialTheme.colorScheme.surface,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.primary,
                    snackbarData = it
                )
            }
        },
        bottomBar = { BottomNavigationBar(navController, selected) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            GoogleMapWithCameraPosition(globeViewModel, hasOpened)
            SearchBar(globeViewModel)
            // If the user has invoked a function causing the sheetState to become visible (clicked
            // search button, clicked enter in TextField, opened screen with permissions enabled or
            // clicked the map), display BottomSheet with weather data at selected location
            if (globeUiState.sheetState.isVisible) {
                BottomSheetWeatherData(globeViewModel)
            }
        }
    }

    val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    // Check if user has missing permissions. If not, don't load user's location by returning early
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        || status != ConnectionResult.SUCCESS
    ) {
        hasOpened = true
        LaunchedEffect(Unit) { globeUiState.sheetState.hide() }
        return
    }

    // Loads data for users location when opening Globe screen. Sometimes takes a bit to load
    // because it waits for success event when asking user for their location.
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (hasOpened || location == null) {
            return@addOnSuccessListener
        }

        val locationLatLng = LatLng(location.latitude, location.longitude)
        coroutineScope.launch {
            val geocodingLocation = getLocationFromCords(locationLatLng)
            globeUiState.cameraPosition = CameraPosition.fromLatLngZoom(locationLatLng, 10f)

            globeViewModel.formatAndSetLocation(geocodingLocation, locationLatLng)
            hasOpened = true
        }
    }
}