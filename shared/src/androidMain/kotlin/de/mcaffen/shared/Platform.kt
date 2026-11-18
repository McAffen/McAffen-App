package de.mcaffen.shared

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

/** Android nutzt den OkHttp-Engine (dieselbe Bibliothek wie die App). */
actual fun defaultEngine(): HttpClientEngine = OkHttp.create()
