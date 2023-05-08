package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import io.mockk.every
import io.mockk.spyk
import kr.hqservice.framework.HQFrameworkPlugin
import kr.hqservice.framework.core.component.registry.ComponentRegistry
import kr.hqservice.framework.core.component.registry.impl.ComponentRegistryImpl
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

class HQFrameworkMock : HQFrameworkPlugin {
    companion object {
        private var plugin: HQFrameworkMock? = null

        fun mock(): HQFrameworkMock {
            if (plugin != null) {
                throw IllegalStateException("already mocking")
            }
            val description = PluginDescriptionFile(
                "HQFramework", "1.0.0",
                HQFrameworkMock::class.java.name
            )
            val instance = MockBukkit.getMock().pluginManager.loadPlugin(HQFrameworkMock::class.java, description, arrayOfNulls(0))
            MockBukkit.getMock().pluginManager.enablePlugin(instance)
            plugin = instance as HQFrameworkMock
            return plugin!!
        }

        fun unmock() {
            ensureMocking()
            MockBukkit.getMock().pluginManager.disablePlugin(plugin!!)
            plugin = null
        }

        private fun ensureMocking() {
            if (plugin == null) {
                throw IllegalStateException("not mocking")
            }
        }
    }

    internal constructor() : super()
    internal constructor(loader: JavaPluginLoader, description: PluginDescriptionFile, dataFolder: File, file: File) : super(loader, description, dataFolder, file)

    override val componentRegistry: ComponentRegistry = spyk(ComponentRegistryImpl(this), recordPrivateCalls = true)

    override fun onPreEnable() {
        stubComponentRepository()
    }

    private fun stubComponentRepository() {
        every { componentRegistry["getAllPluginClasses"]() } returns findTargetClasses()
    }

    private fun findTargetClasses(): Collection<Class<*>> {
        val classes = mutableSetOf<Class<*>>()
        val classLoader = javaClass.classLoader
        val urls = classLoader.getResources("")

        for (url in urls) {
            val directory = File(url.file)
            if (directory.exists()) {
                directory.walk()
                    .filter { f -> f.isFile && !f.name.contains('$') && f.name.endsWith(".class") }
                    .forEach {
                        val fullyQualifiedClassName = it.canonicalPath
                            .removePrefix(directory.canonicalPath)
                            .replace('\\', '/')
                            .replace('/', '.')
                            .removeSuffix(".class")
                            .removePrefix(".")
                        try {
                            val clazz = Class.forName(fullyQualifiedClassName)
                            classes.add(clazz)
                        } catch (exception: ClassNotFoundException) {
                            System.err.println(exception)
                        }
                    }
            }
        }

        return classes
    }
}