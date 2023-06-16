package com.example.nattklar

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nattklar.ui.theme.NattklarTheme
import com.example.nattklar.view.EnterAnimation
import com.example.nattklar.view.globescreen.Globe
import com.example.nattklar.view.homescreen.HomeScreen
import com.example.nattklar.view.newsscreen.News
import com.example.nattklar.view.wikiscreen.ArticleDetail
import com.example.nattklar.view.wikiscreen.ArticleList
import com.example.nattklar.view.wikiscreen.Wiki
import com.example.nattklar.view.wikiscreen.intArgument
import com.example.nattklar.viewmodel.HomeViewModel
import com.example.nattklar.viewmodel.NewsViewModel
import com.example.nattklar.viewmodel.WikiViewModel

class MainActivity: ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Asks user for location permissions. If provided this will load data in the globescreen.
        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            0
        )

        super.onCreate(savedInstanceState)
        setContent {
            NattklarTheme {
                val navController = rememberNavController()

                val homeViewModel = remember { HomeViewModel() }
                val wikiViewModel = remember { WikiViewModel() }
                val newsViewModel = remember { NewsViewModel() }

                homeViewModel.loadArticles(this)
                homeViewModel.loadAstronomicData(this)
                homeViewModel.loadNightEvents(this)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        modifier = Modifier,
                        navController = navController,
                        startDestination = "homeScreen"
                    ) {
                        composable("homeScreen") { EnterAnimation { HomeScreen(navController, homeViewModel, wikiViewModel) }}
                        composable("news") { EnterAnimation { News(navController, newsViewModel) }}
                        composable("globe") { EnterAnimation { Globe(navController) }}
                        composable("wiki") { EnterAnimation { Wiki(navController, wikiViewModel) }}
                        composable("articleList/{topic}") {
                            EnterAnimation { ArticleList(navController, wikiViewModel)}
                        }
                        composable("articleDetail/{articleId}", listOf(intArgument("articleId"))) {
                            EnterAnimation { ArticleDetail(navController, wikiViewModel)}
                        }
                    }
                }
            }
        }
    }
}