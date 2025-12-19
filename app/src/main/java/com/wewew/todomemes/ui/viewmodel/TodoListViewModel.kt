package com.wewew.todomemes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wewew.todomemes.data.local.model.TodoItem
import com.wewew.todomemes.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import javax.inject.Inject

data class TodoListUiState(
    val todos: List<TodoItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val logger = LoggerFactory.getLogger("TodoListViewModel")

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    init {
        logger.info("TodoListViewModel initialized")
        observeTodos()
        loadTodos()
    }

    private fun observeTodos() {
        viewModelScope.launch {
            repository.todosFlow.collect { list ->
                _uiState.update { it.copy(todos = list) }
            }
        }
    }

    fun loadTodos() {
        viewModelScope.launch {
            logger.info("loadTodos() - Starting")
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.loadTodos()
                _uiState.update { it.copy(isLoading = false) }
                logger.info("loadTodos() - Completed")
            } catch (e: Exception) {
                logger.error("loadTodos() - Failed: ${e.message}", e)
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun deleteTodo(item: TodoItem) {
        viewModelScope.launch {
            logger.info("deleteTodo() - Deleting uid=${item.uid}")
            try {
                repository.deleteTodo(item.uid)
            } catch (e: Exception) {
                logger.error("deleteTodo() - Failed: ${e.message}", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleDone(item: TodoItem) {
        viewModelScope.launch {
            logger.info("toggleDone() - Toggling uid=${item.uid}")
            try {
                repository.toggleDone(item)
            } catch (e: Exception) {
                logger.error("toggleDone() - Failed: ${e.message}", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}