package com.wewew.todomemes.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.wewew.todomemes.Importance
import com.wewew.todomemes.TodoItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun TodoItemCard(
    item: TodoItem,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sky = ComposeColor(0xFF9BBFD5)
    val steel = ComposeColor(0xFF6B92B5)
    val aluminum = ComposeColor(0xFFDDDEDB)

    val warmBrown = ComposeColor(0xFF483E38)
    val graphite = ComposeColor(0xFF282322)
    val midGray = ComposeColor(0xFF66615C)
    val dustGray = ComposeColor(0xFF9B9891)
    val nearBlack = ComposeColor(0xFF0E0B0A)

    val accentColor = ComposeColor(item.color)
    val baseContainer = if (item.isDone) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
    } else {
        nearBlack.copy(alpha = 0.98f)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val glowPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlowPhase"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(sky, steel, aluminum, steel, sky),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(620f * glowPhase.value + 220f, 620f)
    )

    val leftStripeBrush = Brush.verticalGradient(
        colors = listOf(accentColor, sky, steel)
    )

    val titleColor = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant else aluminum
    val metaAccent = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant else sky

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = baseContainer),
        border = BorderStroke(1.5.dp, borderBrush),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isDone) 2.dp else 10.dp,
            pressedElevation = 14.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .heightIn(min = 72.dp)
                    .fillMaxHeight()
                    .padding(end = 10.dp)
                    .background(leftStripeBrush, RoundedCornerShape(999.dp))
            )

            Column(
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = item.isDone, onCheckedChange = onCheckedChange)

                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                        color = titleColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ImportanceChip(
                        importance = item.importance,
                        sky = sky,
                        steel = steel,
                        warmBrown = warmBrown,
                        dustGray = dustGray,
                        nearBlack = nearBlack
                    )
                    DeadlineText(
                        deadline = item.deadline,
                        iconTint = metaAccent,
                        textTint = steel,
                        bgA = graphite,
                        bgB = midGray
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportanceChip(
    importance: Importance,
    sky: ComposeColor,
    steel: ComposeColor,
    warmBrown: ComposeColor,
    dustGray: ComposeColor,
    nearBlack: ComposeColor
) {
    val (label, brush, textColor) = when (importance) {
        Importance.LOW -> Triple(
            "низкий",
            Brush.horizontalGradient(listOf(dustGray, sky)),
            nearBlack
        )
        Importance.NORMAL -> Triple(
            "обычный",
            Brush.horizontalGradient(listOf(sky, steel)),
            nearBlack
        )
        Importance.HIGH -> Triple(
            "высокий",
            Brush.horizontalGradient(listOf(warmBrown, steel)),
            ComposeColor(0xFFF3F4F6)
        )
    }

    Box(
        modifier = Modifier
            .heightIn(min = 28.dp)
            .background(brush, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
private fun DeadlineText(
    deadline: Instant?,
    iconTint: ComposeColor,
    textTint: ComposeColor,
    bgA: ComposeColor,
    bgB: ComposeColor
) {
    if (deadline == null) return

    val formatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()) }
    val text = remember(deadline) { formatter.format(deadline.atZone(ZoneId.systemDefault())) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = 28.dp)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(bgA.copy(alpha = 0.55f), bgB.copy(alpha = 0.55f))
                ),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "до $text",
            style = MaterialTheme.typography.bodySmall,
            color = textTint
        )
    }
}
