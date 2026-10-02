package kr.hqservice.framework.yaml

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.extension.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class HQYamlDefaultTest {
    @TempDir
    lateinit var dir: File

    private fun config(content: String = "a: 1\nname: hq\nflag: true\n"): HQYamlConfiguration {
        val file = File(dir, "config.yml")
        file.writeText(content)
        return file.yaml()
    }

    @Test
    fun missingKeysReturnCallerDefault() {
        val config = config()
        assertEquals(7, config.getInt("missingInt", 7))
        assertEquals("x", config.getString("missingString", "x"))
        assertEquals(true, config.getBoolean("missingBoolean", true))
        assertEquals(7L, config.getLong("missingLong", 7L))
        assertEquals(7.5, config.getDouble("missingDouble", 7.5))
        assertEquals(7.5f, config.getFloat("missingFloat", 7.5f))
    }

    @Test
    fun presentKeysWinOverCallerDefault() {
        val config = config()
        assertEquals(1, config.getInt("a", 7))
        assertEquals("hq", config.getString("name", "x"))
        assertEquals(true, config.getBoolean("flag", false))
        assertEquals(1L, config.getLong("a", 7L))
        assertEquals(1.0, config.getDouble("a", 7.5))
        assertEquals(1.0f, config.getFloat("a", 7.5f))
    }

    @Test
    fun reloadReadsTheMostRecentlyLoadedFile() {
        val fileA = File(dir, "a.yml").apply { writeText("a: 1\n") }
        val fileB = File(dir, "b.yml").apply { writeText("a: 2\n") }
        val config = fileA.yaml()
        config.load(fileB)
        assertEquals(2, config.getInt("a"))

        fileB.writeText("a: 3\n")
        config.reload()

        assertEquals(3, config.getInt("a"))
    }
}
