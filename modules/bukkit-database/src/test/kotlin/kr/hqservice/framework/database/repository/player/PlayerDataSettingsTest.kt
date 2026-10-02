package kr.hqservice.framework.database.repository.player

import kr.hqservice.framework.yaml.extension.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Duration

class PlayerDataSettingsTest {
    @TempDir
    lateinit var tempDir: File

    private fun config(text: String) = File(tempDir, "c.yml").apply { writeText(text) }.yaml()

    @Test
    fun `empty config gives defaults`() {
        val settings = PlayerDataSettings.from(config(""))
        assertEquals("database", settings.backend)
        assertEquals(Duration.ofSeconds(30), settings.lease)
        assertEquals(Duration.ofSeconds(10), settings.renewInterval)
        assertEquals(Duration.ofSeconds(5), settings.joinTimeout)
        assertEquals(Duration.ofMillis(200), settings.retryInterval)
        assertEquals(Duration.ofSeconds(5), settings.dirtyFlushInterval)
        assertEquals(Duration.ofSeconds(60), settings.fullFlushInterval)
    }

    @Test
    fun `unsupported backend fails`() {
        assertThrows<IllegalStateException> {
            PlayerDataSettings.from(config("player-data:\n  backend: redis\n"))
        }
    }

    @Test
    fun `lease seconds are read`() {
        val settings = PlayerDataSettings.from(config("player-data:\n  lease-seconds: 45\n"))
        assertEquals(Duration.ofSeconds(45), settings.lease)
    }
}
