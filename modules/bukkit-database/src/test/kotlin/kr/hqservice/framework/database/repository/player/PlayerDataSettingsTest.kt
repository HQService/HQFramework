package kr.hqservice.framework.database.repository.player

import kr.hqservice.framework.yaml.extension.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
            PlayerDataSettings.from(config("player-data:\n  backend: mongo\n"))
        }
    }

    @Test
    fun `redis backend is accepted`() {
        assertEquals("redis", PlayerDataSettings.from(config("player-data:\n  backend: redis\n")).backend)
    }

    @Test
    fun `lease seconds are read`() {
        val settings = PlayerDataSettings.from(config("player-data:\n  lease-seconds: 45\n"))
        assertEquals(Duration.ofSeconds(45), settings.lease)
    }

    private fun assertRejected(key: String, yaml: String) {
        val error = assertThrows<IllegalStateException> { PlayerDataSettings.from(config("player-data:\n$yaml")) }
        assertTrue(error.message!!.contains(key), error.message)
    }

    @Test
    fun `renew interval must be positive`() = assertRejected("renew-seconds", "  renew-seconds: 0\n")

    @Test
    fun `lease must be longer than renew interval`() = assertRejected("lease-seconds", "  lease-seconds: 10\n  renew-seconds: 10\n")

    @Test
    fun `dirty flush interval must be positive`() = assertRejected("dirty-flush-seconds", "  dirty-flush-seconds: 0\n")

    @Test
    fun `full flush interval must not be shorter than dirty flush interval`() =
        assertRejected("full-flush-seconds", "  dirty-flush-seconds: 10\n  full-flush-seconds: 5\n")

    @Test
    fun `join timeout must be positive`() = assertRejected("join-timeout-seconds", "  join-timeout-seconds: 0\n")

    @Test
    fun `retry interval must be positive`() = assertRejected("retry-interval-millis", "  retry-interval-millis: 0\n")
}
