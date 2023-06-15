package com.example.nattklar.view.homescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nattklar.R
import com.example.nattklar.model.round
import com.example.nattklar.viewmodel.HomeViewModel

@Composable
fun UserLocationDisplay(userLocation: Triple<String, Double, Double>, homeViewModel: HomeViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // add this line
    ) {
        if (userLocation.first == "Ingen stedstillatelse" || userLocation.first == "Mangler google play services") {
            Text(
                text = userLocation.first, fontSize = 25.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(80.dp))
        } else {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = userLocation.first,
                    fontSize = 43.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${userLocation.second.round(3)}° N\n" +
                            "${userLocation.third.round(3)}° Ø",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.5.sp,
                    lineHeight = 17.sp
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                val sunPos = if (homeViewModel.isSunset()) {
                    painterResource(R.drawable.sunset)
                } else {
                    painterResource(R.drawable.sunrise)
                }

                Icon(
                    painter = sunPos,
                    contentDescription = "Sunrise",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
                Text(
                    text = homeViewModel.getSolarDisplay(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp
                )
            }
        }
    }
}
