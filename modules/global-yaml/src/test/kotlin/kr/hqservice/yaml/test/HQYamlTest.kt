package kr.hqservice.yaml.test

import kr.hqservice.yaml.extension.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class HQYamlTest {

    @Test
    fun yamlTest() {
        val file = File("src/test/resources/config.yml")
        val yaml = file.yaml()
        assertEquals(yaml.getString("host"), "")
        assertEquals(yaml.getSection("netty")?.getString("host"), "127.0.0.1")

        val temp = File("src/test/resources/temp.yml")
        file.copyTo(temp, true)

        file.appendText("\ntest: hello")
        assertEquals(yaml.getString("test"), "")

        yaml.reload()
        assertEquals(yaml.getString("test"), "hello")

        temp.copyTo(file, true)
        temp.delete()
    }

}