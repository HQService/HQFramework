package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import io.mockk.every
import io.mockk.spyk
import kr.hqservice.framework.HQFrameworkPlugin
import kr.hqservice.framework.core.HQFrameworkModule
import kr.hqservice.framework.core.component.registry.ComponentRegistry
import kr.hqservice.framework.core.component.registry.impl.ComponentRegistryImpl
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.ksp.generated.module
import java.io.File

class HQFrameworkMock : HQFrameworkPlugin {
    companion object {
        private var plugin: HQFrameworkMock? = null
        private val extendedSearchScope: MutableList<Class<*>> = mutableListOf()
        private lateinit var testNames: List<String>

        fun mock(testName: String, extendSearchScope: List<Class<*>> = listOf()): HQFrameworkMock {
            return mock(listOf(testName), extendSearchScope)
        }

        fun mock(testNames: List<String>, extendSearchScope: List<Class<*>> = listOf()): HQFrameworkMock {
            this.testNames = testNames
            this.extendedSearchScope.addAll(extendSearchScope)
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

    public override val componentRegistry: ComponentRegistry = spyk(ComponentRegistryImpl(this), recordPrivateCalls = true)

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
                    .filter { f -> f.isFile && f.name.endsWith(".class") }
                    .forEach {
                        val fullyQualifiedClassName = it.canonicalPath
                            .removePrefix(directory.canonicalPath)
                            .replace('\\', '/')
                            .replace('/', '.')
                            .removeSuffix(".class")
                            .removePrefix(".")
                        try {
                            val clazz = Class.forName(fullyQualifiedClassName)
                            if (clazz.getAnnotation(ExcludeTestSearch::class.java) == null) {
                                val isolated: Isolated? = clazz.getAnnotation(Isolated::class.java)
                                if (isolated == null || testNames.contains(isolated.testName)) {
                                    classes.add(clazz)
                                }
                            }
                        } catch (exception: ClassNotFoundException) {
                            System.err.println(exception)
                        }
                    }
            }
        }

        classes.addAll(extendedSearchScope)
        return classes
    }
}