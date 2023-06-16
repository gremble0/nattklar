package com.example.nattklar.view.wikiscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nattklar.model.dataobjects.articleImages
import com.example.nattklar.viewmodel.WikiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shows text and image for the selected celestial body/bodies.
 */
@Composable
fun ArticleDetail(navController: NavController, wikiViewModel: WikiViewModel) {
    val wikiUiState = wikiViewModel.uiState.collectAsState().value
    val currentArticle = wikiUiState.currentArticle

    val scrollState = rememberLazyListState()
    val backButtonOpacity = remember { mutableStateOf(1f) }
    val threshold = with(LocalDensity.current) { 50.dp.toPx()}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
        if (currentArticle != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState,
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val category = currentArticle.category
                        val title = currentArticle.title

                        Text(
                            text = title ?: "Loading...",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary
                        )

                        val imageId = articleImages[category]?.get(title)

                        imageId?.let {
                            Image(
                                painter = painterResource(imageId),
                                contentDescription = "Article Image",
                                modifier = Modifier
                                    .height(200.dp)
                                    .width(200.dp)
                                    .weight(1f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentArticle.body ?: "Loading...",
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            // Back button blir usynlig dersom man scroller ned og blir synlig etter scrolle tilbake
            LaunchedEffect(scrollState) {
                snapshotFlow { scrollState.firstVisibleItemScrollOffset }
                    .collect { scrollOffset ->
                        withContext(Dispatchers.Main) {
                            val alpha = 1f - (scrollOffset.toFloat() / threshold).coerceIn(0f, 1f)
                            backButtonOpacity.value = alpha
                        }
                    }
            }
        }
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .alpha(backButtonOpacity.value),
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}