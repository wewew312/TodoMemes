package com.wewew.todomemes.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wewew.todomemes.Importance
import com.wewew.todomemes.TodoItem
import com.wewew.todomemes.data.repository.TodoRepository
import com.wewew.todomemes.ui.screens.toArgb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class EditTodoUiState(
    val uid: String = "",

    val text: String = "",
    val importance: Importance = Importance.NORMAL,
    val isDone: Boolean = false,
    val deadline: Instant? = null,
    val selectedColorArgb: Int = android.graphics.Color.WHITE,
    val customColorArgb: Int? = null,

    val showDatePicker: Boolean = false,
    val showColorPicker: Boolean = false,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditTodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val todoId: String? = savedStateHandle.get<String>("todoId")?.takeIf { it != "new" }
    private val newUid = java.util.UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(
        EditTodoUiState(uid = todoId ?: newUid, isLoading = false)
    )
    val uiState: StateFlow<EditTodoUiState> = _uiState.asStateFlow()

    init { loadTodo() }

    private fun loadTodo() {
        viewModelScope.launch {
            if (todoId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val item = repository.getTodoById(todoId)

                val color = item?.color ?: android.graphics.Color.WHITE
                _uiState.value = _uiState.value.copy(
                    uid = item?.uid ?: todoId,
                    text = item?.text.orEmpty(),
                    importance = item?.importance ?: Importance.NORMAL,
                    isDone = item?.isDone ?: false,
                    deadline = item?.deadline,
                    selectedColorArgb = color,
                    customColorArgb = color.takeIf { argb ->
                        com.wewew.todomemes.ui.components.presetColors.none { it.toArgb() == argb }
                    },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onTextChange(v: String) = update { it.copy(text = v, saveSuccess = false) }
    fun onImportanceSelected(v: Importance) = update { it.copy(importance = v, saveSuccess = false) }
    fun onDoneChange(v: Boolean) = update { it.copy(isDone = v, saveSuccess = false) }

    fun onOpenDatePicker() = update { it.copy(showDatePicker = true) }
    fun onDismissDatePicker() = update { it.copy(showDatePicker = false) }
    fun onDeadlineSelected(v: Instant) =
        update { it.copy(deadline = v, showDatePicker = false, saveSuccess = false) }
    fun onClearDeadline() = update { it.copy(deadline = null, saveSuccess = false) }

    fun onOpenColorPicker() = update { it.copy(showColorPicker = true) }
    fun onDismissColorPicker() = update { it.copy(showColorPicker = false) }
    fun onColorSelected(colorArgb: Int, isCustom: Boolean) = update {
        it.copy(
            selectedColorArgb = colorArgb,
            customColorArgb = if (isCustom) colorArgb else null,
            showColorPicker = false,
            saveSuccess = false
        )
    }

    fun onSaveClick() {
        val s = _uiState.value
        if (s.text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                repository.saveTodo(
                    TodoItem(
                        uid = s.uid,
                        text = s.text,
                        importance = s.importance,
                        color = s.selectedColorArgb,
                        deadline = s.deadline,
                        isDone = s.isDone
                    )
                )
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save"
                )
            }
        }
    }

    private inline fun update(block: (EditTodoUiState) -> EditTodoUiState) {
        _uiState.value = block(_uiState.value)
    }
}
