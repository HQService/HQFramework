package kr.hqservice.framework.global.core.component.registry

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.testscan.ScannableSample
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.reflect.KClass

class JarBasedComponentRegistryTest {
    @TempDir
    lateinit var tempDir: File

    private class TestRegistry(
        private val jar: File,
        private val classLoader: ClassLoader
    ) : JarBasedComponentRegistry() {
        override fun getComponentScope(): String = "kr.hqservice.testscan"

        override fun getJar(): File = jar

        override fun getPluginClassLoader(): ClassLoader = classLoader

        override fun getProvidedInstances(): MutableMap<KClass<*>, out Any> = mutableMapOf()

        override fun getConfiguration(): HQYamlConfiguration = throw UnsupportedOperationException()
    }

    private fun createJar(): File {
        val sampleEntry = "kr/hqservice/testscan/ScannableSample.class"
        val sampleBytes = ScannableSample::class.java.getResourceAsStream("/$sampleEntry")!!.use { it.readBytes() }
        val brokenBytes = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte(), 0x00, 0x00, 0x00, 0xFF.toByte()) + ByteArray(32)
        val jar = File(tempDir, "plugin.jar")
        JarOutputStream(jar.outputStream()).use { output ->
            output.putNextEntry(JarEntry(sampleEntry))
            output.write(sampleBytes)
            output.closeEntry()
            output.putNextEntry(JarEntry("kr/hqservice/testscan/Broken.class"))
            output.write(brokenBytes)
            output.closeEntry()
        }
        return jar
    }

    @Test
    fun `scan skips classes the JVM cannot link and keeps the rest`() {
        val jar = createJar()
        URLClassLoader(arrayOf(jar.toURI().toURL()), javaClass.classLoader).use { classLoader ->
            val registry = TestRegistry(jar, classLoader)

            val scanned = registry.getAllComponentsToScan()

            assertTrue(scanned.contains(ScannableSample::class.java))
            assertTrue(scanned.none { it.simpleName == "Broken" })
        }
    }
}
