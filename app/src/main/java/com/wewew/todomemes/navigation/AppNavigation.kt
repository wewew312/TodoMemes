package com.wewew.todomemes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wewew.todomemes.FileStorage
import com.wewew.todomemes.ui.screens.EditTodoScreen
import com.wewew.todomemes.ui.screens.TodoListScreen

sealed class Screen(val route: String) {
    data object TodoList : Screen("todo_list")
    data object EditTodo : Screen("edit_todo/{todoId}") {
        fun createRoute(todoId: String?) = "edit_todo/${todoId ?: "new"}"
    }
}

@Composable
fun AppNavigation(
    fileStorage: FileStorage,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var todos by remember { mutableStateOf(fileStorage.getItems()) }

    fun refreshTodos() {
        todos = fileStorage.getItems()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.TodoList.route,
        modifier = modifier
    ) {
        composable(Screen.TodoList.route) {
            TodoListScreen(
                todos = todos,
                onTodoClick = { todo ->
                    navController.navigate(Screen.EditTodo.createRoute(todo.uid))
                },
                onAddClick = {
                    navController.navigate(Screen.EditTodo.createRoute(null))
                },
                onDeleteTodo = { todo ->
                    fileStorage.remove(todo.uid)
                    fileStorage.save()
                    refreshTodos()
                },
                onToggleDone = { todo ->
                    val updated = todo.copy(isDone = !todo.isDone)
                    fileStorage.add(updated)
                    fileStorage.save()
                    refreshTodos()
                }
            )
        }

        composable(Screen.EditTodo.route) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getString("todoId")
            val existingTodo = if (todoId != null && todoId != "new") {
                todos.find { it.uid == todoId }
            } else null

            EditTodoScreen(
                todoItem = existingTodo,
                onSave = { item ->
                    fileStorage.add(item)
                    fileStorage.save()
                    refreshTodos()
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}