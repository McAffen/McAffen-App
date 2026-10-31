package de.mcaffen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.mcaffen.app.ui.McAffenTheme
import androidx.compose.material3.Surface
import de.mcaffen.app.data.AppContainer
import de.mcaffen.app.ui.McAffenApp

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        setContent {
            McAffenTheme {
                Surface { McAffenApp(container.repository) }
            }
        }
    }
}
