package com.wewew.todomemes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wewew.todomemes.Importance

@Composable
fun ImportanceSelector(
    selectedImportance: Importance,
    onImportanceSelected: (Importance) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Важность:",
            modifier = Modifier.padding(bottom = 8.dp),
            color = if (enabled) Color.Unspecified else Color.Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Importance.entries.forEach { importance ->
                ImportanceChip(
                    importance = importance,
                    isSelected = selectedImportance == importance,
                    onClick = { if (enabled) onImportanceSelected(importance) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ImportanceChip(
    importance: Importance,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !enabled -> Color.LightGray.copy(alpha = 0.3f)
        isSelected -> getImportanceColor(importance)
        else -> Color.Transparent
    }

    val borderColor = when {
        !enabled -> Color.Gray
        isSelected -> getImportanceColor(importance)
        else -> Color.Gray
    }

    val textColor = when {
        !enabled -> Color.Gray
        isSelected -> Color.White
        else -> Color.Black
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = importance.ruName,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

fun getImportanceColor(importance: Importance): Color {
    return when (importance) {
        Importance.LOW -> Color(0xFF4CAF50)
        Importance.NORMAL -> Color(0xFF2196F3)
        Importance.HIGH -> Color(0xFFF44336)
    }
}