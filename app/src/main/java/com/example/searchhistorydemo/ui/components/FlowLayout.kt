package com.example.searchhistorydemo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FlowLayout(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    maxLines: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        
        val horizontalSpacingPx = with(density) { horizontalSpacing.roundToPx() }
        val verticalSpacingPx = with(density) { verticalSpacing.roundToPx() }
        
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        
        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0
        var currentLine = 0
        
        placeables.forEach { placeable ->
            if (currentRow.isEmpty()) {
                currentRow.add(placeable)
                currentRowWidth = placeable.width
            } else {
                if (currentRowWidth + horizontalSpacingPx + placeable.width <= constraints.maxWidth) {
                    currentRow.add(placeable)
                    currentRowWidth += horizontalSpacingPx + placeable.width
                } else {
                    rows.add(currentRow.toList())
                    currentLine++
                    if (currentLine >= maxLines) {
                        return@forEach
                    }
                    currentRow = mutableListOf(placeable)
                    currentRowWidth = placeable.width
                }
            }
        }
        
        if (currentRow.isNotEmpty() && currentLine < maxLines) {
            rows.add(currentRow.toList())
        }
        
        val totalHeight = rows.size * (placeables.firstOrNull()?.height ?: 0) +
                (rows.size - 1) * verticalSpacingPx
        
        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalSpacingPx
                }
                y += (row.firstOrNull()?.height ?: 0) + verticalSpacingPx
            }
        }
    }
}

@Composable
fun FlowLayoutWithLineCount(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    maxLines: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
): Int {
    val lineCountState = remember { mutableStateOf(1) }

    val density = androidx.compose.ui.platform.LocalDensity.current

    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->

        val horizontalSpacingPx = with(density) { horizontalSpacing.roundToPx() }
        val verticalSpacingPx = with(density) { verticalSpacing.roundToPx() }

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            if (currentRow.isEmpty()) {
                currentRow.add(placeable)
                currentRowWidth = placeable.width
            } else {
                if (currentRowWidth + horizontalSpacingPx + placeable.width <= constraints.maxWidth) {
                    currentRow.add(placeable)
                    currentRowWidth += horizontalSpacingPx + placeable.width
                } else {
                    rows.add(currentRow.toList())
                    currentRow = mutableListOf(placeable)
                    currentRowWidth = placeable.width
                }
            }
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow.toList())
        }

        lineCountState.value = rows.size

        val totalHeight = rows.size * (placeables.firstOrNull()?.height ?: 0) +
                (rows.size - 1) * verticalSpacingPx

        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalSpacingPx
                }
                y += (row.firstOrNull()?.height ?: 0) + verticalSpacingPx
            }
        }
    }

    return lineCountState.value
}
