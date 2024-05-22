package kr.hqservice.framework.yaml.test

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.extension.yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class HQYamlTest {
    
    private lateinit var file: File
    private lateinit var config: HQYamlConfiguration
    
    @BeforeEach
    fun setup() {
        file = File("src/test/resources/config.yml")
        config = file.yaml()
    }
    
    @Test
    fun configTest() {
        assertEquals(config.getString("host"), "")
        assertEquals(config.getSection("netty")?.getString("host"), "127.0.0.1")

        val temp = File("src/test/resources/temp.yml")
        file.copyTo(temp, true)

        file.appendText("\ntest: hello")
        assertEquals(config.getString("test"), "")

        config.reload()
        config.getSection("netty")?.getKeys()?.forEach {
            // it.print("netty-section-> ")
        }
        assertEquals(config.getString("test"), "hello")

        temp.copyTo(file, true)
        temp.delete()
    }

    @Test
    fun get_test() {
        val strings = config.getStringList("string-list")
        val doubles = config.getDoubleList("double-list")
        val floats = config.getFloatList("float-list")
        val longs = config.getLongList("long-list")
        val ints = config.getIntegerList("int-list")
        println(strings)
        println(doubles)
        println(longs)
        println(floats)
        println(ints)
    }
    
    @Test
    fun contains_test() {
        println("float contains: ${config.contains("float")}")
        println("test contains: ${config.contains("contains.test")}")
        println("test2 contains: ${config.contains("contains.test2")}")
    }
}