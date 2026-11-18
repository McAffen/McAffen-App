package de.mcaffen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.mcaffen.app.data.AppContainer
import de.mcaffen.app.data.UpdateCheckScheduler
import de.mcaffen.app.ui.McAffenApp
import de.mcaffen.app.ui.McAffenTheme
import de.mcaffen.app.ui.ThemeOptions

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        // Startbildschirm mit Markenlogo (androidx.core.splashscreen, API 21+).
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        // Android 13+: Benachrichtigungen einmalig erlauben lassen, damit die
        // Update-Meldung wirklich ankommt.
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 7)
            }
        }
        container = AppContainer(applicationContext)
        // Regelmäßige Hintergrund-Update-Prüfung einrichten (oder abmelden,
        // wenn automatische Prüfungen ausgeschaltet sind).
        UpdateCheckScheduler.update(applicationContext)
        // Zusätzlich eine sofortige Prüfung: liegt ein neues Release vor, kommt
        // die Update-Benachrichtigung, ohne dass man die App öffnen muss.
        UpdateCheckScheduler.checkNow(applicationContext)
        setContent {
            val darkMode by container.settings.darkMode.collectAsStateWithLifecycle()
            val color by container.settings.color.collectAsStateWithLifecycle()
            // Edge-to-Edge: Systemleisten passend zum gewählten Hell/Dunkel-Modus färben.
            val isDark = when (darkMode) {
                ThemeOptions.DARK_LIGHT -> false
                ThemeOptions.DARK_DARK -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            McAffenTheme(color = color, darkMode = darkMode) {
                Surface { McAffenApp(container.repository, container.settings) }
            }
            // Icon-Farbe der Systemleisten nach Modus setzen (ohne Neustart der Activity).
            androidx.compose.runtime.SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                )
            }
        }
    }
}
