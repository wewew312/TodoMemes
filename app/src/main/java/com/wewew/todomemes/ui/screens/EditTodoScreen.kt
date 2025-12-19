package com.wewew.todomemes.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wewew.todomemes.Importance
import com.wewew.todomemes.TodoItem
import com.wewew.todomemes.ui.components.ColorPicker
import com.wewew.todomemes.ui.components.ColorSelector
import com.wewew.todomemes.ui.components.ImportanceSelector
import com.wewew.todomemes.ui.components.presetColors
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoScreen(
    todoItem: TodoItem?,
    onSave: (TodoItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(todoItem?.text ?: "") }
    var importance by remember { mutableStateOf(todoItem?.importance ?: Importance.NORMAL) }
    var isDone by remember { mutableStateOf(todoItem?.isDone ?: false) }
    var deadline by remember { mutableStateOf(todoItem?.deadline) }
    var selectedColor by remember {
        mutableStateOf(Color(todoItem?.color ?: AndroidColor.WHITE))
    }

    var customColor by remember {
        mutableStateOf<Color?>(
            if (todoItem != null && !presetColors.contains(Color(todoItem.color))) {
                Color(todoItem.color)
            } else null
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val contentAlpha = if (isDone) 0.5f else 1f

    Scaffold(
        modifier = modifier,
        topBar = {
            EditTodoTopBar(
                onBack = onBack,
                onSave = {
                    val item = TodoItem(
                        uid = todoItem?.uid ?: java.util.UUID.randomUUID().toString(),
                        text = text,
                        importance = importance,
                        color = selectedColor.toArgb(),
                        deadline = deadline,
                        isDone = isDone
                    )
                    onSave(item)
                },
                canSave = text.isNotBlank()
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
                text = text,
                onTextChange = { text = it },
                enabled = !isDone
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            DeadlineSection(
                deadline = deadline,
                onDeadlineClick = { showDatePicker = true },
                onClearDeadline = { deadline = null },
                enabled = !isDone
            )
            Spacer(modifier = Modifier.height(16.dp))

            DoneSection(
                isDone = isDone,
                onDoneChange = { isDone = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            ImportanceSelector(
                selectedImportance = importance,
                onImportanceSelected = { importance = it },
                enabled = !isDone
            )

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            ColorSelector(
                selectedColor = selectedColor,
                customColor = customColor,
                onColorSelected = { selectedColor = it },
                onCustomColorClick = { showColorPicker = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        TodoDatePickerDialog(
            initialDate = deadline,
            onDateSelected = { deadline = it },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showColorPicker) {
        Dialog(
            onDismissRequest = { showColorPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                ColorPicker(
                    initialColor = customColor ?: selectedColor,
                    onColorSelected = { color ->
                        customColor = color
                        selectedColor = color
                        showColorPicker = false
                    },
                    onDismiss = { showColorPicker = false }
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