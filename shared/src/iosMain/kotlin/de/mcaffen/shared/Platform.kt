package de.mcaffen.shared

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/** iOS nutzt NSURLSession über den Darwin-Engine von Ktor. */
actual fun defaultEngine(): HttpClientEngine = Darwin.create()
