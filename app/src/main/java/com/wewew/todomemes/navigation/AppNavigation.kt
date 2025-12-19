package com.wewew.todomemes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.TodoList.route,
        modifier = modifier
    ) {
        composable(Screen.TodoList.route) {
            TodoListScreen(
                onTodoClick = { todo ->
                    navController.navigate(Screen.EditTodo.createRoute(todo.uid))
                },
                onAddClick = {
                    navController.navigate(Screen.EditTodo.createRoute(null))
                }
            )
        }

        composable(Screen.EditTodo.route) {
            EditTodoScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
