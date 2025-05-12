
package com.federico.mylibrary.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PieChartSection(data: Map<String, Int>) {
    val total = data.values.sum().toFloat()
    Canvas(modifier = Modifier
        .height(250.dp)
        .fillMaxWidth()
    ) {
        var startAngle = 0f
        val radius = size.minDimension / 2.2f
        val center = this.center

        data.entries.forEachIndexed { index, (label, count) ->
            val sweep = 360f * (count.toFloat() / total)
            drawArc(
                color = themedColor(index),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = center.copy(
                    x = center.x - radius,
                    y = center.y - radius
                )
            )
            startAngle += sweep
        }
    }
}

@Composable
fun Legend(data: Map<String, Int>, defaultLabel: String = "Non rilevato") {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        data.keys.forEachIndexed { index, label ->
            val displayLabel = if (label.isBlank()) defaultLabel else label.replaceFirstChar { it.uppercase() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(themedColor(index), shape = MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(displayLabel)
            }
        }
    }
}

private val softColors = listOf(
    Color(0xFF81D4FA), // Light Blue
    Color(0xFFA5D6A7), // Light Green
    Color(0xFFFFCC80), // Orange
    Color(0xFFCE93D8), // Purple
    Color(0xFFFFAB91), // Coral
    Color(0xFFB0BEC5), // Gray Blue
    Color(0xFFE6EE9C), // Lime
    Color(0xFFFFF59D), // Yellow
    Color(0xFFB39DDB), // Lavender
    Color(0xFF80CBC4)  // Teal
)

fun themedColor(index: Int): Color {
    return softColors[index % softColors.size]
}
