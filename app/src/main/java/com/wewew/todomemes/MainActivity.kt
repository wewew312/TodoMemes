package com.wewew.todomemes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wewew.todomemes.navigation.AppNavigation
import com.wewew.todomemes.ui.theme.TodoMemesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val fileStorage = FileStorage(context = this)
        fileStorage.load()

        setContent {
            TodoMemesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        fileStorage = fileStorage,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}