package com.example.searchhistorydemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.searchhistorydemo.model.HistoryItem
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchHistoryView(
    historyItems: List<HistoryItem>,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    onItemClick: (HistoryItem) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var totalLineCount by remember { mutableStateOf(1) }
    var containerWidth by remember { mutableStateOf(0) }

    val density = LocalDensity.current
    val fontFamilyResolver = androidx.compose.ui.platform.LocalFontFamilyResolver.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            },
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (containerWidth > 0) {
            totalLineCount = calculateLineCount(
                items = historyItems,
                containerWidth = containerWidth,
                horizontalSpacing = horizontalSpacing,
                verticalSpacing = verticalSpacing,
                density = density,
                fontFamilyResolver = fontFamilyResolver
            )
        }

        if (totalLineCount <= 2) {
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                verticalArrangement = Arrangement.spacedBy(verticalSpacing)
            ) {
                historyItems.forEach { item ->
                    HistoryChip(
                        text = item.text,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        } else {
            InlineExpandCollapseFlowRow(
                modifier = Modifier.weight(1f),
                items = historyItems,
                isExpanded = isExpanded,
                horizontalSpacing = horizontalSpacing,
                verticalSpacing = verticalSpacing,
                onItemClick = onItemClick,
                onToggleExpand = { isExpanded = !isExpanded }
            )
        }
    }
}

@Composable
fun HistoryChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            color = Color(0xFF333333),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExpandCollapseButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (isExpanded) "收起" else "展开",
            modifier = Modifier
                .padding(6.dp)
                .size(24.dp),
            tint = Color(0xFF2196F3)
        )
    }
}

@Composable
fun InlineExpandCollapseFlowRow(
    modifier: Modifier = Modifier,
    items: List<HistoryItem>,
    isExpanded: Boolean,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    onItemClick: (HistoryItem) -> Unit = {},
    onToggleExpand: () -> Unit = {}
) {
    val density = LocalDensity.current

    Layout(
        content = {
            items.forEach { item ->
                HistoryChip(
                    text = item.text,
                    onClick = { onItemClick(item) }
                )
            }
            ExpandCollapseButton(
                isExpanded = isExpanded,
                onClick = onToggleExpand
            )
        },
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalSpacingPx = with(density) { horizontalSpacing.roundToPx() }
        val verticalSpacingPx = with(density) { verticalSpacing.roundToPx() }

        val chipMeasurables = measurables.dropLast(1)
        val buttonMeasurable = measurables.last()

        val chipPlaceables = chipMeasurables.map { it.measure(androidx.compose.ui.unit.Constraints()) }
        val buttonPlaceable = buttonMeasurable.measure(androidx.compose.ui.unit.Constraints())

        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentRowWidth = 0

        chipPlaceables.forEach { placeable ->
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

        val finalRows = mutableListOf<List<Placeable>>()

        if (isExpanded) {
            rows.forEach { row ->
                finalRows.add(row.toList())
            }
            val lastRow = finalRows.last().toMutableList()
            val buttonWidth = buttonPlaceable.width
            val currentRowWidthPx = lastRow.sumOf { it.width } + (lastRow.size - 1) * horizontalSpacingPx

            if (currentRowWidthPx + horizontalSpacingPx + buttonWidth <= constraints.maxWidth) {
                lastRow.add(buttonPlaceable)
                finalRows[finalRows.size - 1] = lastRow.toList()
            } else {
                finalRows.add(listOf(buttonPlaceable))
            }
        } else {
            val displayRows = rows.take(2)
            displayRows.forEachIndexed { index, row ->
                if (index < displayRows.size - 1) {
                    finalRows.add(row.toList())
                } else {
                    val lastRow = row.toMutableList()
                    val buttonWidth = buttonPlaceable.width
                    val currentRowWidthPx = lastRow.sumOf { it.width } + (lastRow.size - 1) * horizontalSpacingPx

                    if (currentRowWidthPx + horizontalSpacingPx + buttonWidth <= constraints.maxWidth) {
                        lastRow.add(buttonPlaceable)
                        finalRows.add(lastRow.toList())
                    } else {
                        val adjustedRow = mutableListOf<Placeable>()
                        var adjustedWidth = 0
                        for (placeable in lastRow) {
                            if (adjustedWidth + horizontalSpacingPx + placeable.width + horizontalSpacingPx + buttonWidth <= constraints.maxWidth) {
                                adjustedRow.add(placeable)
                                adjustedWidth += placeable.width + horizontalSpacingPx
                            } else {
                                break
                            }
                        }
                        adjustedRow.add(buttonPlaceable)
                        finalRows.add(adjustedRow.toList())
                    }
                }
            }
        }

        val totalHeight = finalRows.size * (chipPlaceables.firstOrNull()?.height ?: 0) +
                (finalRows.size - 1) * verticalSpacingPx

        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            finalRows.forEach { row ->
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

private fun calculateLineCount(
    items: List<HistoryItem>,
    containerWidth: Int,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    density: androidx.compose.ui.unit.Density,
    fontFamilyResolver: androidx.compose.ui.text.font.FontFamily.Resolver
): Int {
    val horizontalSpacingPx = with(density) { horizontalSpacing.roundToPx() }
    val chipHeight = with(density) { 32.dp.roundToPx() }
    
    var currentLineWidth = 0
    var lineCount = 1
    
    items.forEach { item ->
        val chipWidth = estimateChipWidth(item.text, density, fontFamilyResolver)
        
        if (currentLineWidth == 0) {
            currentLineWidth = chipWidth
        } else if (currentLineWidth + horizontalSpacingPx + chipWidth <= containerWidth) {
            currentLineWidth += horizontalSpacingPx + chipWidth
        } else {
            lineCount++
            currentLineWidth = chipWidth
        }
    }
    
    return lineCount
}

private fun estimateChipWidth(
    text: String,
    density: androidx.compose.ui.unit.Density,
    fontFamilyResolver: androidx.compose.ui.text.font.FontFamily.Resolver
): Int {
    val textWidth = with(density) {
        androidx.compose.ui.text.TextMeasurer(
            defaultFontFamilyResolver = fontFamilyResolver,
            defaultDensity = density,
            defaultLayoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr
        ).measure(
            text = androidx.compose.ui.text.AnnotatedString(text),
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp
            )
        ).size.width
    }
    val paddingWidth = with(density) { 24.dp.roundToPx() }
    return textWidth + paddingWidth
}
