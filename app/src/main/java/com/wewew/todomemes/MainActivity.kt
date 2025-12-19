package com.wewew.todomemes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wewew.todomemes.ui.screens.EditTodoScreen
import com.wewew.todomemes.ui.theme.TodoMemesTheme
import org.slf4j.LoggerFactory

class MainActivity : ComponentActivity() {
    private val logger = LoggerFactory.getLogger("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val fileStorage = FileStorage(context = this)
        fileStorage.load()

        setContent {
            var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
            var showEditor by remember { mutableStateOf(true) }

            TodoMemesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showEditor) {
                        EditTodoScreen(
                            todoItem = editingTodo,
                            onSave = { item ->
                                fileStorage.add(item)
                                fileStorage.save()
                                logger.info("Saved todo: ${item.text}")
                                editingTodo = item
                            },
                            onBack = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}