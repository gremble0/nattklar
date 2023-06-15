package com.example.nattklar.view.homescreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.nattklar.model.dataprocessing.GoogleMaps
import com.example.nattklar.view.BottomNavigationBar
import com.example.nattklar.viewmodel.HomeViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.example.nattklar.viewmodel.WikiViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.CoroutineScope


/**
 * The HomeScreen composable function that contains the UI for the main screen.
 *
 * This function uses both a WikiViewModel and NewsViewModel to retrieve data for display.
 * It includes logic to handle different states of the UI including loading, permission
 * checks and displaying the actual content. The UI includes a scaffold with a bottom
 * navigation bar, a loading screen, a title bar and a pager with news and constellation cards.
 *
 * @param navController NavController to handle navigation events in the app
 * @param wikiViewModel ViewModel to navigate to articles in the wiki data for the app
 * @param homeViewModel ViewModel to handle data for the homescreen
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel, wikiViewModel: WikiViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userLocation = rememberLocation(context, coroutineScope, homeViewModel)
    val homeUiState by homeViewModel.uiState.collectAsState()
    val articles = homeUiState.constellationArticlesShuffled

    if (articles.isEmpty() || homeUiState.news.isEmpty()  || userLocation.first == "") {
        LoadingScreen()
    } else {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController, "homeScreen") }
        ) {
            Box(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(it)
            ) {
                // initialPage is set to an absurdly large number to have an illusion of infinite scroll
                // It is then divided in 2 to set the instialPage to be in the middle for the effect
                val pagerStateConstellation = rememberPagerState(initialPage = Int.MAX_VALUE / 2)
                val pagerStateNews = rememberPagerState(initialPage = 0)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    UserLocationDisplay(userLocation, homeViewModel)
                    Spacer(Modifier.height(10.dp))
                    HomeScreenNewsCard(pagerStateNews, homeUiState)
                    Spacer(Modifier.height(16.dp))
                    HomeScreenConstellationCards(
                        pagerStateConstellation,
                        articles,
                        navController,
                        homeViewModel,
                        wikiViewModel
                    )
                }
            }
        }
    }
}

/**
 * Composable function that retrieves the user's location and coordinates using
 * the FusedLocationProviderClient. Requires ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
 * permissions.
 *
 * @param context Current context
 * @param coroutineScope Coroutine scope for performing the location retrieval asynchronously
 * @param homeViewModel ViewModel to handle data for the homescreen
 * @return a Triple of location name and coordinates (latitude, longitude)
 */
@Composable
fun rememberLocation(context: Context, coroutineScope: CoroutineScope, homeViewModel: HomeViewModel): Triple<String, Double, Double> {
    val userLocationStateFlow = remember { MutableStateFlow(Triple("", 0.0, 0.0)) }
    var userLocation = userLocationStateFlow.collectAsState().value

    val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    if (status != ConnectionResult.SUCCESS) {
        userLocation = Triple("Mangler google play services", 0.0, 0.0)
    }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        userLocation = Triple("Ingen stedstillatelse",0.0,0.0)
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        var geocodingLocation: String
        val locationLatLng: LatLng?


        if (location == null) {
            return@addOnSuccessListener
        }

        locationLatLng = LatLng(location.latitude, location.longitude)
        coroutineScope.launch {
            homeViewModel.loadSolarProperties(locationLatLng)
            geocodingLocation = GoogleMaps.getLocationFromCords(locationLatLng).toString().split(",")[0]
            userLocationStateFlow.value = Triple(
                geocodingLocation,
                locationLatLng.latitude,
                locationLatLng.longitude
            )

        }
    }
    return userLocation
}
/**
 * Composable function that displays a loading screen with a CircularProgressIndicator.
 */
@Composable
fun LoadingScreen(){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nattklar",
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary
        )
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.surface
        )
    }
}