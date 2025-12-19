package com.wewew.todomemes.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val presetColors = listOf(
    Color.White,
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorSelector(
    selectedColor: Color,
    customColor: Color?,
    onColorSelected: (Color) -> Unit,
    onCustomColorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Цвет:",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetColors.forEach { color ->
                ColorSquare(
                    color = color,
                    isSelected = selectedColor == color && customColor == null,
                    onClick = { onColorSelected(color) }
                )
            }

            CustomColorSquare(
                customColor = customColor,
                isSelected = customColor != null && selectedColor == customColor,
                onClick = {
                    customColor?.let { onColorSelected(it) }
                },
                onLongClick = onCustomColorClick
            )
        }
    }
}

@Composable
fun ColorSquare(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Выбрано",
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomColorSquare(
    customColor: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (customColor != null) {
                    Modifier.background(customColor)
                } else {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta,
                                Color.Red
                            )
                        )
                    )
                }
            )
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(4.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected && customColor != null) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Выбрано",
                tint = if (customColor.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}