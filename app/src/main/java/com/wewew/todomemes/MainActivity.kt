package com.wewew.todomemes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.wewew.todomemes.analysis.LeakSimulator
import com.wewew.todomemes.ui.theme.TodoMemesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val activity = this
            TodoMemesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LeakDemoScreen(
                        activity = activity,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LeakDemoScreen(activity: ComponentActivity, modifier: Modifier = Modifier) {
    var leakInfo by remember { mutableStateOf("No leaks yet") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Leak Canary Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка утечки Activity
        Button(
            onClick = {
                LeakSimulator.leakActivity(activity)
                leakInfo = LeakSimulator.getLeakInfo()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Leak Activity (Static Reference)")
        }

        // Кнопка утечки памяти
        Button(
            onClick = {
                LeakSimulator.leakMemory()
                leakInfo = LeakSimulator.getLeakInfo()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Leak 1MB Memory")
        }

        // Кнопка утечки через Handler
        Button(
            onClick = {
                LeakSimulator.leakWithHandler(activity)
                leakInfo = LeakSimulator.getLeakInfo()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Leak with Handler (5 min)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка очистки
        Button(
            onClick = {
                LeakSimulator.cleanup()
                leakInfo = LeakSimulator.getLeakInfo()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cleanup All Leaks")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Статус утечек
        Text(
            text = leakInfo,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "После нажатия на красные кнопки:\n" +
                    "1. Поверните экран или закройте приложение\n" +
                    "2. LeakCanary покажет уведомление об утечке\n" +
                    "3. Нажмите на уведомление для деталей",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LeakDemoPreview() {
    TodoMemesTheme {
        Text("Preview not available - requires Activity")
    }
}