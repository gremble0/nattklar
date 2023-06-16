package com.example.nattklar.view.wikiscreen

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nattklar.model.dataobjects.articleImages
import com.example.nattklar.model.dataobjects.Article
import com.example.nattklar.viewmodel.WikiViewModel

/**
 * Shows a scrollable list of article rows.
 */
@Composable
fun ArticleList(navController: NavController, wikiViewModel: WikiViewModel) {
    val wikiUiState = wikiViewModel.uiState.collectAsState().value
    val articles = wikiUiState.categorisedArticles
    
    Scaffold (
        content = {
            Box(modifier = Modifier.padding(it)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(articles) { article ->
                        ArticleRow(article, navController, wikiViewModel)

                    }
                }
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}

/**
 * Shows article row card with its corresponding icon and name for its celestial body.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ArticleRow(article: Article, navController: NavController, wikiViewModel: WikiViewModel) {
    var selected by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(if (selected) 1.1f else 1f, label = "")

    Card(
        modifier = Modifier
            .padding(13.dp)
            .width(300.dp)
            .height(80.dp)
            .scale(scale.value)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Scale the card up when touched.
                        selected = true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Reset the card scale and navigate to the article list screen.
                        selected = false
                        wikiViewModel.setCurrentArticle(article)
                        navController.navigate("articleDetail/${article.id}") {
                            popUpTo("articleList") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // In case the card is not pressed, it will reset the card scale
                        selected = false
                    }
                }
                true
            },
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        navController.navigate("articleDetail/${article.id}") {
                            popUpTo("articleList") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val imageId = articleImages[article.category]?.get(article.title)

                imageId?.let {
                    Image(
                        painter = painterResource(imageId),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(3.dp, 3.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
                Text(
                    text = article.title ?: "Loading...",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 16.dp, end= 16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}
