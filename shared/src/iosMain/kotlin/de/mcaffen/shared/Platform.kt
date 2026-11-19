package de.mcaffen.shared

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/** iOS nutzt den Darwin-Engine (NSURLSession). */
actual fun defaultEngine(): HttpClientEngine = Darwin.create()
