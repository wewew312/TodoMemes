package com.wewew.todomemes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wewew.todomemes.ui.components.ColorPicker
import com.wewew.todomemes.ui.components.ColorSelector
import com.wewew.todomemes.ui.components.ImportanceSelector
import com.wewew.todomemes.ui.viewmodel.EditTodoViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditTodoViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    val selectedColor = Color(ui.selectedColorArgb)
    val customColor = ui.customColorArgb?.let { Color(it) }

    val contentAlpha = if (ui.isDone) 0.5f else 1f

    Scaffold(
        modifier = modifier,
        topBar = {
            EditTodoTopBar(
                onBack = onBack,
                onSave = {
                    viewModel.onSaveClick()
                    onBack.invoke()
                },
                canSave = ui.text.isNotBlank() && !ui.isSaving
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .alpha(contentAlpha)
        ) {
            TextInputSection(
                text = ui.text,
                onTextChange = viewModel::onTextChange,
                enabled = !ui.isDone
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            DeadlineSection(
                deadline = ui.deadline,
                onDeadlineClick = viewModel::onOpenDatePicker,
                onClearDeadline = viewModel::onClearDeadline,
                enabled = !ui.isDone
            )

            Spacer(Modifier.height(16.dp))
            DoneSection(
                isDone = ui.isDone,
                onDoneChange = viewModel::onDoneChange
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            ImportanceSelector(
                selectedImportance = ui.importance,
                onImportanceSelected = viewModel::onImportanceSelected,
                enabled = !ui.isDone
            )

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            ColorSelector(
                selectedColor = selectedColor,
                customColor = customColor,
                onColorSelected = { viewModel.onColorSelected(it.toArgb(), isCustom = false) },
                onCustomColorClick = viewModel::onOpenColorPicker
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    if (ui.showDatePicker) {
        TodoDatePickerDialog(
            initialDate = ui.deadline,
            onDateSelected = viewModel::onDeadlineSelected,
            onDismiss = viewModel::onDismissDatePicker
        )
    }

    AnimatedColorPickerDialog(
        visible = ui.showColorPicker,
        initialColor = customColor ?: selectedColor,
        onColorSelected = { viewModel.onColorSelected(it.toArgb(), isCustom = true) },
        onDismiss = viewModel::onDismissColorPicker
    )
}

@Composable
fun AnimatedColorPickerDialog(
    visible: Boolean,
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ),
        exit = fadeOut(tween(150)) + scaleOut(
            animationSpec = tween(150),
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + slideOutVertically(
            animationSpec = tween(150),
            targetOffsetY = { it / 2 }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(16.dp)
            ) {
                ColorPicker(
                    initialColor = initialColor,
                    onColorSelected = onColorSelected,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    TopAppBar(
        title = { Text("Редактирование") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSave,
                enabled = canSave
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Сохранить"
                )
            }
        }
    )
}

@Composable
fun TextInputSection(
    text: String,
    onTextChange: (String) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = "Дело о",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            placeholder = { Text("Введите текст задачи...") },
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            maxLines = Int.MAX_VALUE,
            singleLine = false
        )
    }
}

@Composable
fun DoneSection(
    isDone: Boolean,
    onDoneChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Дело сделано",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = isDone,
            onCheckedChange = onDoneChange
        )
    }
}

@Composable
fun DeadlineSection(
    deadline: Instant?,
    onDeadlineClick: () -> Unit,
    onClearDeadline: () -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = "Дедлайн:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = if (enabled) Color.Unspecified else Color.Gray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                onClick = { if (enabled) onDeadlineClick() },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Text(
                        text = deadline?.let { formatDate(it) } ?: "Не указан",
                        color = if (enabled) Color.Unspecified else Color.Gray
                    )
                }
            }

            if (deadline != null && enabled) {
                TextButton(onClick = onClearDeadline) {
                    Text("Очистить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDatePickerDialog(
    initialDate: Instant?,
    onDateSelected: (Instant) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toEpochMilli() ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Instant.ofEpochMilli(it))
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

fun formatDate(instant: Instant): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    return formatter.format(Date.from(instant))
}

fun Color.toArgb(): Int {
    return AndroidColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}