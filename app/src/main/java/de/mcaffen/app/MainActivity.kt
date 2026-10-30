package de.mcaffen.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import de.mcaffen.app.data.AppContainer
import de.mcaffen.app.ui.McAffenApp

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        setContent {
            MaterialTheme {
                Surface { McAffenApp(container.repository) }
            }
        }
        handleConnectorIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleConnectorIntent(intent)
    }

    private fun handleConnectorIntent(intent: Intent?) {
        if (intent?.data == Uri.parse("app://connector_openai_deep_research")) {
            // The connector launches this activity; the UI remains available for follow-up research.
        }
    }
}
