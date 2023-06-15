package com.example.nattklar.view.wikiscreen

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nattklar.view.BottomNavigationBar
import com.example.nattklar.viewmodel.WikiViewModel
import com.example.nattklar.model.dataobjects.getTopicIconResource

/**
 * Shows the wiki screen with a scrollable list of topic rows.
 */
@Composable
fun Wiki(navController: NavController, wikiViewModel: WikiViewModel) {

    wikiViewModel.setArticles()

    val wikiUiState by wikiViewModel.uiState.collectAsState()
    val topics =  wikiUiState.articles.map {it.category}.distinct()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, "wiki") }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("Wiki",
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.primary)
            }
            items(topics) { topic ->
                TopicRow(topic!!, navController, wikiViewModel)
            }
        }
    }
}


/**
 * Shows topic row card with icon and name for its specified topic.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopicRow(topic: String, navController: NavController, wikiViewModel: WikiViewModel) {
    val selected = remember { mutableStateOf(false) }
    // Animate the scale value based on the card's selection state.
    val scale = animateFloatAsState(if (selected.value) 1.1f else 1f, label = "")

    Card(
        modifier = Modifier
            .padding(10.dp)
            .width(300.dp)
            .height(90.dp)
            .scale(scale.value)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Scale the card up when touched.
                        selected.value = true
                    }

                    MotionEvent.ACTION_UP -> {
                        // Reset the card scale and navigate to the article list screen.
                        wikiViewModel.setNewArticleCategory(topic)
                        selected.value = false
                        navController.navigate("articleList/$topic")
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // In case the card is not pressed, it will reset the card scale
                        selected.value = false
                    }
                }
                true
            },
        // Set the card's shape and elevation.
        shape = RoundedCornerShape(40.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .clickable {
                        navController.navigate("articleList/$topic")
                    }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = getTopicIconResource(topic)),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(3.dp, 3.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
                )
                Text(
                    text = topic,
                    fontSize = 23.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}