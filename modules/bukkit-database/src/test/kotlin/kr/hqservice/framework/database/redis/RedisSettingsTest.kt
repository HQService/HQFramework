package kr.hqservice.framework.database.redis

import kr.hqservice.framework.yaml.extension.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Duration

class RedisSettingsTest {
    @TempDir
    lateinit var tempDir: File

    private fun config(text: String) = File(tempDir, "c.yml").apply { writeText(text) }.yaml()

    @Test
    fun `empty config gives disabled defaults`() {
        val settings = RedisSettings.from(config(""))
        assertFalse(settings.enabled)
        assertEquals("", settings.uri)
        assertEquals("hq", settings.keyPrefix)
        assertEquals(Duration.ofSeconds(3600), settings.dataTtl)
    }

    @Test
    fun `configured uri enables redis`() {
        val settings = RedisSettings.from(config("redis:\n  uri: redis://127.0.0.1:6379/0\n  key-prefix: srv\n  data-ttl-seconds: 60\n"))
        assertTrue(settings.enabled)
        assertEquals("redis://127.0.0.1:6379/0", settings.uri)
        assertEquals("srv", settings.keyPrefix)
        assertEquals(Duration.ofSeconds(60), settings.dataTtl)
    }

    @Test
    fun `key joins prefix and parts`() {
        val settings = RedisSettings.from(config(""))
        assertEquals("hq:session:x", settings.key("session", "x"))
    }

    @Test
    fun `data ttl must be positive`() {
        val error = assertThrows<IllegalStateException> {
            RedisSettings.from(config("redis:\n  data-ttl-seconds: 0\n"))
        }
        assertTrue(error.message!!.contains("data-ttl-seconds"), error.message)
    }
}
