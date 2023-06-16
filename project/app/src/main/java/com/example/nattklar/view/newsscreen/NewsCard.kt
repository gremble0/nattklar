package com.example.nattklar.view.newsscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nattklar.model.dataobjects.NightEvent
import com.example.nattklar.model.dataobjects.nightEventsImages
import com.example.nattklar.model.getDate
import com.example.nattklar.model.getNorwegianTime
import com.example.nattklar.model.getTimeOfDay
import com.example.nattklar.model.parseToUTC

/**
 * Shows a news card with [Image] and [Text] based on the type of the [nightEvent] news.
 */
@Composable
fun NewsCard(nightEvent: NightEvent) {
    val newsType: String = nightEvent.eventType

    Card(
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary),
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(5.dp)
        ) {
            val imageId = nightEventsImages[newsType]
            imageId?.let {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = "Nyhetsbilde",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(125.dp)
                        .clip(RoundedCornerShape(10))
                )
            }
            Column {
                Text(
                    text = nightEvent.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = nightEvent.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 10.dp, bottom = 10.dp),
                    color = MaterialTheme.colorScheme.primary

                )
                Text(
                    text = fixDateAndTimeFormat(nightEvent.whenShown),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * @return the provided [dateString] as "<date>, kl. <time>".
 */
private fun fixDateAndTimeFormat(dateString: String): String {
    val timeInUTC = parseToUTC(dateString)
    val norwegianTime = getNorwegianTime(timeInUTC)

    val date = getDate(norwegianTime, split = ".")
    val clockTime = getTimeOfDay(norwegianTime)

    return "$date, kl. $clockTime"
}