package com.example.nattklar.view.newsscreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nattklar.model.dataobjects.NightEvent
import com.example.nattklar.view.BottomNavigationBar
import com.example.nattklar.viewmodel.NewsViewModel

/**
 * Shows a title component and a number of [NewsCard]s with information about various [NightEvent]s.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun News(navController: NavController, newsViewModel: NewsViewModel) {
    newsViewModel.setNightEvents()
    val selected by remember { mutableStateOf("news") }
    val newsUiState by newsViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, selected)
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Text("Nyheter",
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )
            LazyColumn {
                items(newsUiState.news.asReversed()) { item ->
                    NewsCard(nightEvent = item)
                }
            }
        }
    }
}
