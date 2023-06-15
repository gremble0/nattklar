
package com.example.nattklar.view.globescreen.weatherwindow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Shows a display informing the user that location data is still in the process of being retrieved.
 */
@Composable
fun LoadingWeatherDataDisplay() {
    val spaceBetween = 8.dp
    val travelDistance = 15.dp

    val circles = listOf(
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) },
    )

    // Loops through each circle Animatable() and animates them.
    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(key1 = animatable) {
            // Each Animatable circle starts 100 milliseconds after eachother (100ms * (0|1|2))
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        // Each of the following lines is a keyframe indicating the circles position at x ms
                        // into animation. E.g. circle reaches the targetvalue (1.0f) at 300ms into animation.
                        durationMillis = 1200
                        0.0f at 0 with LinearOutSlowInEasing
                        1.0f at 300 with LinearOutSlowInEasing
                        0.0f at 600 with LinearOutSlowInEasing
                        0.0f at 1200 with LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    val circleValues = circles.map { it.value }
    val distance = with(LocalDensity.current) { travelDistance.toPx() }
    val lastCircle = circleValues.size - 1

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Laster inn værdata", color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.size(20.dp))

        Row {
            circleValues.forEachIndexed { index, value ->
                Box(modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { translationY = -value * distance }
                    .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                )

                if (index != lastCircle) {
                    Spacer(modifier = Modifier.width(spaceBetween))
                }
            }
        }
    }
}