package de.mcaffen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.mcaffen.app.data.AppContainer
import de.mcaffen.app.data.UpdateCheckScheduler
import de.mcaffen.app.ui.McAffenApp
import de.mcaffen.app.ui.McAffenTheme

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        // Regelmäßige Hintergrund-Update-Prüfung einrichten (oder abmelden,
        // wenn automatische Prüfungen ausgeschaltet sind).
        UpdateCheckScheduler.update(applicationContext)
        setContent {
            val darkMode by container.settings.darkMode.collectAsStateWithLifecycle()
            val color by container.settings.color.collectAsStateWithLifecycle()
            McAffenTheme(color = color, darkMode = darkMode) {
                Surface { McAffenApp(container.repository, container.settings) }
            }
        }
    }
}
