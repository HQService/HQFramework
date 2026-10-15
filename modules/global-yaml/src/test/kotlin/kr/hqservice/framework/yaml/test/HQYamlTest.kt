package kr.hqservice.framework.yaml.test

import kr.hqservice.framework.yaml.extension.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class HQYamlTest {

    @Test
    fun yamlTest() {
        val file = File("src/test/resources/config.yml")
        val original = file.readText()
        try {
            val yaml = file.yaml()
            assertEquals("", yaml.getString("host"))
            assertEquals("127.0.0.1", yaml.getSection("netty")?.getString("host"))

            file.appendText("\ntest: hello")
            assertEquals("", yaml.getString("test"))

            yaml.reload()
            assertEquals("hello", yaml.getString("test"))
        } finally {
            file.writeText(original)
        }
    }
}
