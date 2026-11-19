package de.mcaffen.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Läuft auf Android (JVM) und im iOS-Simulator – ohne echtes Gerät. */
class UpdateLogicTest {

    @Test
    fun newerVersionIsDetected() {
        assertTrue(UpdateLogic.isNewer("1.0.5", "v1.0.6"))
        assertTrue(UpdateLogic.isNewer("1.0.9", "1.0.10"))
    }

    @Test
    fun sameOrOlderVersionIsNotNewer() {
        assertFalse(UpdateLogic.isNewer("1.0.6", "v1.0.6"))
        assertFalse(UpdateLogic.isNewer("1.0.10", "1.0.9"))
    }

    @Test
    fun releaseJsonIsParsed() {
        val json = """
            {"tag_name":"v1.0.7","html_url":"https://example.org/r","body":"- Neu",
             "assets":[{"name":"McAffen-release.apk","browser_download_url":"https://example.org/a.apk","size":42}]}
        """.trimIndent()
        val info = UpdateLogic.newerRelease(json, "1.0.6")
        assertNotNull(info)
        assertEquals("1.0.7", info.version)
        assertEquals("https://example.org/a.apk", info.assetUrl)
    }

    @Test
    fun draftAndOldReleasesAreIgnored() {
        assertNull(UpdateLogic.newerRelease("""{"tag_name":"v1.0.9","draft":true}""", "1.0.6"))
        assertNull(UpdateLogic.newerRelease("""{"tag_name":"v1.0.6"}""", "1.0.6"))
    }

    @Test
    fun iosDoesNotRequireApk() {
        val json = """{"tag_name":"v1.0.7","html_url":"https://example.org/r"}"""
        assertNotNull(UpdateLogic.newerRelease(json, "1.0.6", requireApk = false))
        assertNull(UpdateLogic.newerRelease(json, "1.0.6", requireApk = true))
    }

    @Test
    fun noteLinesStripMarkdown() {
        val lines = UpdateLogic.noteLines("## Was ist neu\n- **Fett** und `Code`\n\n[Link](https://x.y)")
        assertEquals(listOf("Was ist neu", "- Fett und Code", "Link"), lines)
    }

    @Test
    fun lanAddressMatchesHostAndPort() {
        assertEquals("bedrock.mcaffen.de:19132", LanInfo.BEDROCK_ADDRESS)
    }
}
