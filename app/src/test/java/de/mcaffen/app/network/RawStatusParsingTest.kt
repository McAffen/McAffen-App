package de.mcaffen.app.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RawStatusParsingTest {
    private val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter(RawStatus::class.java)

    @Test
    fun currentApiFormatWithPlayerNumbers() {
        val json = """
            {"onlinePlayers":2,"networkServers":10,
             "messages":["Wartung"],
             "servers":[
               {"name":"Lobby","type":"backend","online":true,"players":1,"version":"Purpur 1.21.11"},
               {"name":"SkyBlock","type":"backend","online":false,"players":0,"version":"offline"}]}
        """.trimIndent()
        val status = adapter.fromJson(json)!!
        assertEquals(2, status.onlinePlayers)
        assertEquals(10, status.networkServers)
        assertEquals(listOf("Wartung"), status.messages)
        assertEquals(listOf(1, 0), status.servers.map { it.playerCount })
    }

    @Test
    fun legacyFormatWithPlayerObjects() {
        val json = """{"servers":[{"name":"Lobby","online":true,"players":{"online":3,"max":20}}]}"""
        val status = adapter.fromJson(json)!!
        assertEquals(3, status.servers.single().playerCount)
        assertTrue(status.messages.isEmpty())
    }
}
