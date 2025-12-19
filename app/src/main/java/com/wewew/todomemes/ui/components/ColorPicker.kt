package com.wewew.todomemes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.wewew.todomemes.ui.screens.toArgb

@Composable
fun ColorPicker(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }

    val currentColor = remember(hue, saturation, brightness) {
        Color.hsv(hue, saturation, brightness)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(currentColor)
                    .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            )

            Text(
                text = "Выбор цвета",
                color = Color.Black
            )

            Box(modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Яркость: ${(brightness * 100).toInt()}%")
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorGradientPicker(
            brightness = brightness,
            selectedHue = hue,
            selectedSaturation = saturation,
            onColorPicked = { newHue, newSaturation ->
                hue = newHue
                saturation = newSaturation
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }

            Button(
                onClick = { onColorSelected(currentColor) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Выбрать")
            }
        }
    }
}

@Composable
fun ColorGradientPicker(
    brightness: Float,
    selectedHue: Float,
    selectedSaturation: Float,
    onColorPicked: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var pickerSize by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val hue = (offset.x / size.width) * 360f
                        val saturation = 1f - (offset.y / size.height)
                        onColorPicked(
                            hue.coerceIn(0f, 360f),
                            saturation.coerceIn(0f, 1f)
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val hue = (change.position.x / size.width) * 360f
                        val saturation = 1f - (change.position.y / size.height)
                        onColorPicked(
                            hue.coerceIn(0f, 360f),
                            saturation.coerceIn(0f, 1f)
                        )
                    }
                }
        ) {
            pickerSize = Offset(size.width, size.height)

            val hueColors = (0..360 step 30).map { hue ->
                Color.hsv(hue.toFloat(), 1f, brightness)
            }

            drawRect(
                brush = Brush.horizontalGradient(hueColors)
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 1f - brightness),
                        Color.Transparent
                    )
                )
            )

            val crossX = (selectedHue / 360f) * size.width
            val crossY = (1f - selectedSaturation) * size.height

            drawCircle(
                color = Color.White,
                radius = 16.dp.toPx(),
                center = Offset(crossX, crossY),
                style = Stroke(width = 3.dp.toPx())
            )

            drawCircle(
                color = Color.Black,
                radius = 14.dp.toPx(),
                center = Offset(crossX, crossY),
                style = Stroke(width = 2.dp.toPx())
            )

            drawCircle(
                color = Color.hsv(selectedHue, selectedSaturation, brightness),
                radius = 10.dp.toPx(),
                center = Offset(crossX, crossY)
            )
        }
    }
}