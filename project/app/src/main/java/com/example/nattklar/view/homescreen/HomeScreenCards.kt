package com.example.nattklar.view.homescreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.nattklar.model.dataobjects.articleImages
import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nattklar.model.dataobjects.Article
import com.example.nattklar.model.dataobjects.HomeUiState
import com.example.nattklar.viewmodel.HomeViewModel
import com.example.nattklar.viewmodel.WikiViewModel

// TODO: RENAME - has same name as in newsscreen/
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenNewsCard(pagerStateNews: PagerState, homeUiState: HomeUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp),
        //elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary)
    ) {
        HorizontalPager(
            state = pagerStateNews,
            pageCount = homeUiState.news.size
        ) { index ->
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.78f)
                    .width(350.dp)
                    .padding(bottom = 10.dp, top = 10.dp),
                contentAlignment = Alignment.Center
            )
            {
                Text(
                    // Need summary for smaller box
                    text = homeUiState.news[index].shortDescription,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(homeUiState.news.size) { iteration ->
                val color =
                    if (pagerStateNews.currentPage == iteration) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenConstellationCards(
    pagerStateConstellation: PagerState,
    shuffled: List<Article>,
    navController: NavController,
    homeViewModel: HomeViewModel,
    wikiViewModel: WikiViewModel,
){
    val itemCount = shuffled.size

    HorizontalPager(
        state = pagerStateConstellation,
        pageCount = Int.MAX_VALUE,
        pageSize = PageSize.Fixed(300.dp),
        contentPadding = PaddingValues(start = 50.dp, end = 50.dp)
    ) { index ->
        val currentCard = index % itemCount
        val isSelected = pagerStateConstellation.currentPage % itemCount == currentCard

        Box(
            modifier = Modifier
                .fillMaxSize(.98f)
                .fillMaxWidth()
                .scale(
                    animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.8f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        ), label = ""
                    ).value
                )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary)
            ) {
                ConstellationCard(
                    homeViewModel = homeViewModel,
                    article = shuffled[currentCard],
                    onCardClick = {
                        wikiViewModel.setArticles()
                        wikiViewModel.setCurrentArticle(shuffled[currentCard])
                        navController.navigate("articleDetail/${shuffled[currentCard].id}")
                    }
                )
            }
        }
    }
}


/**
 * Shows a card with an image of a specific constellation and a snippet of information about
 * the constellation in a Text underneath the image.
 */
@Composable
fun ConstellationCard(
    homeViewModel: HomeViewModel,
    article: Article,
    onCardClick: () -> Unit
) {
    val articleTitle = article.title!!
    val articleDescription = article.body!!
    val articleCategory = article.category!!
    val articleConstellation = article.constellation!!

    //find first two sentences of the article description
    val sentences = articleDescription.split(".")
    val shortenedDescription = sentences.subList(0,2).joinToString(".") + "."

    var showFullImage by remember { mutableStateOf(false) }
    val imageHeight by animateDpAsState(
        targetValue = if (showFullImage) (LocalConfiguration.current.screenHeightDp.dp) else (LocalConfiguration.current.screenHeightDp / 1.9).dp,
        animationSpec = tween(durationMillis = 400), label = ""
    )

    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary)
    ) {
        Column {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                val imageId = articleImages[articleCategory]?.get(articleTitle)

                imageId?.let {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clip(RoundedCornerShape(4.dp))
                            .pointerInput(Unit) {
                                // If tapped on the image, image will scale to the whole card
                                detectTapGestures(onTap = {
                                    showFullImage = !showFullImage
                                })
                            }
                            .drawWithCache { // This a gradient for the image. From top to bottom will be transparent and black
                                val gradient = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black),
                                    startY = size.height / 3,
                                    endY = size.height
                                )
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(gradient, blendMode = BlendMode.Multiply)
                                }
                            },
                        painter = painterResource(imageId),
                        contentDescription = articleDescription,
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop,
                    )
                }
                Text(
                    text = articleTitle,
                    fontSize = 25.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, bottom = 4.dp)
                        .background(color = MaterialTheme.colorScheme.secondary)

                )
                val visibility = homeViewModel.getConstellationVisibility(articleConstellation, LatLng(60.0,10.0))
                Text(
                    text = "$visibility synlighet",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 4.dp)
                        .background(color = MaterialTheme.colorScheme.secondary),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box(modifier = Modifier
                .padding(8.dp)
                .clickable(onClick = onCardClick)) {
                val scrollState = rememberLazyListState()
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .customVerticalScrollbar(state = scrollState)
                        .padding(end = 16.dp)
                ) {
                    items(listOf(shortenedDescription)) { desc ->
                        Text(
                            text = desc,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 5.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shows a custom vertical scrollbar in the textbox underneath the constellation image.
 */
fun Modifier.customVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    height: Dp = 30.dp,
    alwaysVisible: Boolean = true
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress || alwaysVisible) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration), label = ""
    )

    drawWithContent {
        drawContent()

        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        if (needDrawScrollbar) {
            val totalHeight = size.height
            val range = totalHeight - height.toPx()

            // Calculate scrollbar position
            val totalItemsCount = state.layoutInfo.totalItemsCount
            val visibleItemsInfo = state.layoutInfo.visibleItemsInfo
            val firstVisibleItemIndex = state.firstVisibleItemIndex
            val firstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset

            val scrollProgress = if (visibleItemsInfo.isNotEmpty()) {
                val firstVisibleItemSize = visibleItemsInfo.first().size
                (firstVisibleItemIndex * firstVisibleItemSize + firstVisibleItemScrollOffset) / (totalItemsCount * firstVisibleItemSize - totalHeight)
            } else {
                0f
            }

            val scrollbarOffsetY = range * scrollProgress

            // Draw the scrollbar
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), height.toPx()),
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2),
                alpha = alpha
            )
        }
    }
}
