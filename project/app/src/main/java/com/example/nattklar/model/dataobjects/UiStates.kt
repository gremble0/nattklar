package com.example.nattklar.model.dataobjects

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import com.google.android.gms.maps.model.CameraPosition

/**
 * This data class keeps track of values used for showing data in the globescreen part of the view
 * in the MVVM.
 */
data class GlobeUiState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val searchSnackbar: SnackbarHostState,
    var cameraPosition: CameraPosition,
    var sheetState: SheetState,
    var loadingData: Boolean,
    var forecastAtLocation: ForecastAtLocation?,
    var nightDatas: List<DataAtLocationForNight?>?,
    var nightNr: Int?,
    var graphInfoBoxIndex: Int?,
    var lightPollutionIndex: Int?,
)

/**
 * This data class keeps track of values used for showing data in the newsscreen part of the view
 * in the MVVM.
 */
data class NewsUiState(
    var news: List<NightEvent>,
)

/**
 * This data class keeps track of values used for showing data in the wikiscreen part of the view
 * in the MVVM.
 */
data class WikiUiState(
    var articles: List<Article>,
    var categorisedArticles: List<Article>,
    var currentArticle: Article?,
)

data class HomeUiState(
    var constellationArticlesShuffled: List<Article>,
    var news : List<NightEvent>,
    var solarProperties: SolarProperties?,
    var articlesLoading: Boolean,
)