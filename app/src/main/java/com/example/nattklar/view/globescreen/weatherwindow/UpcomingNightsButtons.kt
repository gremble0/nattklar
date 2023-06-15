package com.example.nattklar.view.globescreen.weatherwindow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nattklar.model.getDayOfWeek
import com.example.nattklar.model.getNorwegianTime
import com.example.nattklar.viewmodel.GlobeViewModel

/**
 * Shows five buttons used to navigate between the 5 upcoming nights. If a button of a night with
 * missing data is clicked, the navigation will be suppressed.
 */
@Composable
fun UpcomingNightsButtons(globeViewModel: GlobeViewModel) {
    val globeUiState by globeViewModel.uiState.collectAsState()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Spacer(modifier = Modifier.height(30.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ){
        for (i in 0..4) {
            Button(
                modifier = Modifier
                    .width(screenWidth / 5 - 3.dp)
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (i == globeUiState.nightNr) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondary
                ),
                onClick = {
                    globeViewModel.setNightNr(i)
                    globeViewModel.setGraphInfoBoxIndex(null)
                  },
                shape = RoundedCornerShape(5.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(unbounded = true),
                        text = (
                            if (globeUiState.nightDatas == null) ""
                            else if (globeUiState.nightDatas!![i] == null) ""
                            else if (i == 0) "I natt"
                            else getDayOfWeek(
                                getNorwegianTime(globeUiState.nightDatas!![i]!!.night)
                            )
                        ),
                        fontSize = 16.sp,
                        style = TextStyle(color = MaterialTheme.colorScheme.primary)
                    )
                    Image(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .size(screenWidth / 6),
                        painter = painterResource(id = globeViewModel.getSymbolForDay(i)),
                        contentDescription = "sunset icon",
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}
