package com.example.nattklar.view.globescreen.weatherwindow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nattklar.R
import com.example.nattklar.model.round
import com.example.nattklar.model.getNorwegianTime
import com.example.nattklar.model.getTimeOfDay
import com.example.nattklar.viewmodel.GlobeViewModel

/**
 * Shows a display containing the location, latitude and longitude, and sunset time of night, of a location.
 */
@Composable
fun LocationDisplay(globeViewModel: GlobeViewModel) {
    val globeUiState by globeViewModel.uiState.collectAsState()

    val sunsetTime = (
        if (globeUiState.nightNr == null) null
        else if (globeUiState.nightDatas!![globeUiState.nightNr!!] == null) null
        else if (globeUiState.nightDatas!![globeUiState.nightNr!!]!!.solarProperties.sunset == null) {
            // If the sun never sets that could also mean that it never rose - we check both cases here
            if (globeUiState.nightDatas!![globeUiState.nightNr!!]!!.solarProperties.solarMidnightElevation > 0.0) "Midnattsol"
            else "Polarnatt"
        }
        else getTimeOfDay(
            getNorwegianTime(globeUiState.nightDatas!![globeUiState.nightNr!!]!!.solarProperties.sunset!!)
        )
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape = RoundedCornerShape(3.dp))
            .fillMaxWidth(0.95f)
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            var locationDescription = globeUiState.forecastAtLocation?.locationDescription?.location
            if (locationDescription != null && locationDescription.length > 13) {
                locationDescription = locationDescription.take(13) + "..."
            }
            Text(
                text = locationDescription ?: " ... ",
                style = TextStyle(color = MaterialTheme.colorScheme.primary),
                fontSize = 45.sp,
            )
            Text(
                text = "${globeUiState.forecastAtLocation?.locationDescription?.cords?.latitude?.round(3)}° N\n" +
                       "${globeUiState.forecastAtLocation?.locationDescription?.cords?.longitude?.round(3)}° Ø",
                style = TextStyle(color = MaterialTheme.colorScheme.primary),
                fontSize = 18.sp,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column (
            modifier = Modifier
                .width(20.dp)
                .wrapContentSize(unbounded = true)
        ){
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.sunset),
                contentScale = ContentScale.FillHeight,
                contentDescription = "sunset icon",
            )
            sunsetTime?.let {
                Text(
                    text = it,
                    style = TextStyle(color = MaterialTheme.colorScheme.primary),
                    fontSize = 22.sp,
                )
            }
        }
        Spacer(modifier = Modifier.width(55.dp))
    }
}