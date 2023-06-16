package com.example.nattklar.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nattklar.R

/**
 * Shows a navigation bar at the bottom of the screen with four navigation options.
 */
@Composable
fun BottomNavigationBar(navController: NavController, selectedRoute: String) {
    val bottomNavItems = listOf(
        BottomNavItem(
            name = stringResource(R.string.bottomNavBar_homeScreen),
            route = "homeScreen",
            icon = painterResource(id = R.drawable.telescope_button)
        ),
        BottomNavItem(
            name = stringResource(R.string.bottomNavBar_news),
            route = "news",
            icon = painterResource(id = R.drawable.news_button)
        ),
        BottomNavItem(
            name = stringResource(R.string.bottomNavBar_globe),
            route = "globe",
            icon = painterResource(id = R.drawable.map_button)
        ),
        BottomNavItem(
            name = stringResource(R.string.bottomNavBar_wiki),
            route = "wiki",
            icon = painterResource(id = R.drawable.wiki_button)
        ),
    )

    Surface(
        modifier = Modifier.shadow(18.dp)
    ) {
        Column (modifier = Modifier.background(color = MaterialTheme.colorScheme.tertiary)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = selectedRoute == item.route

                    // Dersom en av navItems-ene blir trykket skal den lyse opp samme som teksten
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary

                    CompositionLocalProvider(
                        // When an icon is clicked the icon and the text get a ripple effect?
                        // TODO: I cant understand the sentence below, i tried to translate but correct it if its wrong
                        // Når ikonet blir trykket blir bare ikke ikonet ha en ripple effekt men teksten og
                        LocalIndication provides rememberRipple(bounded = false)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                                .padding(top = 10.dp)
                        ) {
                            Icon(
                                item.icon,
                                item.name,
                                modifier = Modifier.size(40.dp),
                                tint = contentColor
                            )
                            // Name taken from values/strings.xml
                            if (isSelected) {
                                Text(
                                    text = item.name,
                                    color = contentColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shows a navigation effect animation when a navigation bar button is clicked.
 */
@Composable
fun EnterAnimation(content: @Composable AnimatedVisibilityScope.() -> Unit) {
    val isVisible = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        isVisible.value = true
    }

    AnimatedVisibility(
        visible = isVisible.value,
        enter = slideInVertically(initialOffsetY = { it })
                + fadeIn(initialAlpha = 0.3f, animationSpec = tween(durationMillis = 500)),
        exit = slideOutVertically() + fadeOut(),
        content = content
    )
}

/**
 * This data class holds relevant data for items on the navigation bar.
 */
data class BottomNavItem(
    val name: String, // Name of the button
    val route: String, // Destination to the screen
    val icon: Painter,
)