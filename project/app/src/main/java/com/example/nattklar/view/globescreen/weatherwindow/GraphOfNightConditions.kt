package com.example.nattklar.view.globescreen.weatherwindow

import android.graphics.Typeface
import androidx.compose.material3.Text
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import android.graphics.Color as GraphicsColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.nattklar.R
import com.example.nattklar.model.dataobjects.NightConditionsSummary
import com.example.nattklar.model.getClockHour
import com.example.nattklar.model.getDayOfWeek
import com.example.nattklar.model.getTimeOfDay
import com.example.nattklar.viewmodel.GlobeViewModel
import kotlin.random.Random.Default.nextFloat

/**
 * Shows a graph showing temperature, cloud area fraction and wind speed of a location and night.
 */
@Composable
fun GraphOfWeatherConditions(
    globeViewModel: GlobeViewModel,
    conditions: NightConditionsSummary,
) {
    Box {
        // Necessary for some usages in this file as MaterialTheme.colorScheme cant get used from outside composables
        val colorScheme = MaterialTheme.colorScheme

        val globeUiState by globeViewModel.uiState.collectAsState()

        // define dimensions for the canvas containing the graphs.
        val configuration = LocalConfiguration.current

        val screenWidth = configuration.screenWidthDp.dp
        val screenWidthPx = with(LocalDensity.current) { screenWidth.toPx() }

        val spacing = 50f

        val figureWidth = screenWidthPx - 2 * spacing
        val figureHeight = screenWidthPx / 3

        // collect relative x- and y-positions.
        val numPoints = conditions.times.size
        val xDiff = figureWidth/numPoints
        val xVals = List(numPoints) { (it + 0.5f) * xDiff }

        val yTemps = conditions.tempData.map {
            calculateRelativeYValues(it, conditions.minTemp, conditions.maxTemp) * figureHeight
        }

        val yClouds = conditions.cloudData.map {
            calculateRelativeYValues(it,0,100) * figureHeight
        }
        val yWindSpeed = conditions.windData.map {
            calculateRelativeYValues(it, conditions.minWind, conditions.maxWind) * figureHeight
        }

        // create rectangles associated with certain x-values. These rectangles are used to detect
        // clicks within them and show an info box.
        val xRects = xVals.map {
            Rect(top = 40.0f, left = it - xDiff / 2, bottom = figureHeight, right = it + xDiff / 2)
        }

        // define anonymous function detecting whether a point is within a rectangle
        val createClickableRects = { tapOffset: Offset ->
            for ((index, rect) in xRects.withIndex()) {
                if (!rect.contains(tapOffset)) continue

                if (globeUiState.graphInfoBoxIndex != index) globeViewModel.setGraphInfoBoxIndex(index)
                else globeViewModel.setGraphInfoBoxIndex(null)

                break
            }
        }
        val updatedClickableRects = rememberUpdatedState(createClickableRects)

        val tempColor = colorResource(R.color.graph_temperature)
        val cloudsColor = colorResource(R.color.graph_clouds)
        val windSpeedColor = colorResource(R.color.graph_wind_speed)
        val markedRectangleColor = colorResource(R.color.graph_marked_background)

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(all = 16.dp)
        ){
            // create figure with drawable items
            Canvas(
                modifier = Modifier
                    .width(screenWidth)
                    .height(screenWidth / 3)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            // register rectangle clicks associated with x value
                            onTap = { tapOffset -> updatedClickableRects.value.invoke(tapOffset) }
                        )
                    }
            ) {
                val selectedIndex = globeUiState.graphInfoBoxIndex

                // define paint for the clock-hours
                val textHeight = 27f
                val timePaint = Paint().asFrameworkPaint().apply {
                    textSize = textHeight
                    color = GraphicsColor.parseColor("#FFfafafa")
                    typeface = Typeface.create("Calibri",Typeface.NORMAL)
                }

                // show clock-hours of the night
                for (i in xVals.indices) {
                    val clockHour = getClockHour(conditions.times[i])
                    val textWidth = timePaint.measureText(clockHour,0,clockHour.length)

                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            clockHour,
                            xRects[i].left+(xDiff-textWidth)/2,
                            textHeight+1f,
                            timePaint
                        )
                    }
                }

                // draw selected area
                selectedIndex?.let {
                    drawRect(
                        color = markedRectangleColor,
                        topLeft = xRects[selectedIndex].topLeft,
                        size = Size(
                            width = xRects[selectedIndex].width,
                            height = xRects[selectedIndex].height,
                        ),
                    )
                }

                // define the graphs to be visualized
                val graphs = mutableListOf(
                    GraphParameters(yTemps,tempColor,createStrokePath(xVals, yTemps)),
                    GraphParameters(yClouds,cloudsColor,createStrokePath(xVals, yClouds)),
                    GraphParameters(yWindSpeed,windSpeedColor,createStrokePath(xVals, yWindSpeed))
                )

                for (graph in graphs) {
                    val yVals = graph.yVals

                    // draw graph path
                    drawPath(
                        path = graph.strokedPath,
                        color = graph.lineColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // draw graph datapoint
                    xVals.indices.forEach {
                        drawCircle(
                            color = graph.lineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(xVals[it], yVals[it])
                        )
                        drawCircle(
                            color = Color.Gray,
                            radius = 2.dp.toPx(),
                            center = Offset(xVals[it], yVals[it])
                        )
                    }
                }

                // draw info box
                selectedIndex?.let {
                    // define paint parameters to the title and values
                    val textHeightF = 40f
                    val infoBoxTitlePaint = Paint().asFrameworkPaint().apply {
                        textSize = textHeightF
                        typeface = Typeface.create("Arial",Typeface.BOLD)
                    }
                    val infoBoxValuesPaint = Paint().asFrameworkPaint().apply {
                        textSize = textHeightF
                        typeface = Typeface.create("Arial",Typeface.NORMAL)
                    }

                    // prepare the strings to be drawn, and find the maximum width of the text
                    val valuesStringformatted = listOf(
                        "Temperatur: ${conditions.tempData[selectedIndex]} °",
                        "Skydekke: ${conditions.cloudData[selectedIndex]} %",
                        "Vindstyrke: ${conditions.windData[selectedIndex]} m/s"
                    )

                    val textWidth = valuesStringformatted.maxOf { infoBoxValuesPaint.measureText(it,0,it.length) }

                    // add a randomness factor to the placement of the info box
                    val r1 = nextFloat()
                    val r2 = nextFloat()

                    // calculate box positions
                    val boxX = (
                        if (xRects[selectedIndex].left+textWidth<figureWidth) {
                            xRects[selectedIndex].left + 50f + r1 * 30f
                        } else {
                            xRects[selectedIndex].right - 50f - r1 * 30f - textWidth
                        }
                    )
                    val boxY = xRects[selectedIndex].top+0.1f*r2*xRects[selectedIndex].bottom

                    // create info box
                    drawRect(
                        color = colorScheme.secondary,
                        topLeft = Offset(boxX - 10, boxY - 10),
                        size = Size(
                            width = textWidth + 20,
                            height = (1+valuesStringformatted.size)*textHeightF + 20
                        ),
                    )
                    // Draw border
                    drawRect(
                        color = colorScheme.surface,
                        topLeft = Offset(boxX - 10, boxY - 10),
                        size = Size(
                            width = textWidth + 20,
                            height = (1+valuesStringformatted.size)*textHeightF + 20
                        ),
                        style = Stroke(
                            width = 1.dp.toPx(),
                        )
                    )

                    // draw the title and containing values of the info box
                    val time = conditions.times[selectedIndex]
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            "${getDayOfWeek(time)} kl. ${getTimeOfDay(time)}",
                            boxX,
                            boxY+textHeightF-1f,
                            infoBoxTitlePaint,
                        )
                    }
                    for (i in valuesStringformatted.indices) {
                        drawIntoCanvas {
                            it.nativeCanvas.drawText(
                                /* text =  */  valuesStringformatted[i],
                                /* x =     */  boxX,
                                /* y =     */  boxY+(i+2)*textHeightF,
                                /* paint = */ infoBoxValuesPaint,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // create graph labels
            Row(
                modifier = Modifier
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.tertiary),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // create temperature label
                Box(
                    modifier = Modifier
                        .background(tempColor)
                        .size(10.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text="Temperatur",
                    style = TextStyle(color = MaterialTheme.colorScheme.primary),
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.width(10.dp))

                // create cloud label
                Box(
                    modifier = Modifier
                        .background(cloudsColor)
                        .size(10.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text="Skydekke",
                    style = TextStyle(color = MaterialTheme.colorScheme.primary),
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.width(10.dp))

                // create air quality label
                Box(
                    modifier = Modifier
                        .background(windSpeedColor)
                        .size(10.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text="Vindstyrke",
                    style = TextStyle(color = MaterialTheme.colorScheme.primary),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

/**
 * @return a stroke path from the two provided list: [xVals] and [yVals].
 */
fun createStrokePath(xVals: List<Float>, yVals: List<Float>): Path {
    val strokePath = Path().apply {
        for (i in xVals.indices) {
            if (i == 0) {
                moveTo(xVals[i], yVals[i])
            } else {
                val conX1 = (xVals[i - 1] + xVals[i]) / 2f
                val conX2 = (xVals[i - 1] + xVals[i]) / 2f

                val conY1 = yVals[i - 1]
                val conY2 = yVals[i]

                cubicTo(
                    x1 = conX1,
                    y1 = conY1,
                    x2 = conX2,
                    y2 = conY2,
                    x3 = xVals[i],
                    y3 = yVals[i]
                )
            }
        }
    }
    return strokePath
}

/**
 * @return the relative y values from [yVal], [minVal] and [maxVal] with some spacing to the edges.
 * Also cases where [maxVal] = [minVal].
 */
fun calculateRelativeYValues(yVal: Int, minVal: Int, maxVal: Int): Float {
    if (minVal == maxVal) return 0.20f + 0.7f * 0.5f
    return 0.20f + 0.70f * (1.0f - (yVal - minVal).toFloat() / (maxVal - minVal).toFloat())

}

/**
 * This data class is used to store graph parameters useful for iteration through each graph.
 */

data class GraphParameters(
    val yVals: List<Float>,
    val lineColor: Color,
    val strokedPath: Path,
)
