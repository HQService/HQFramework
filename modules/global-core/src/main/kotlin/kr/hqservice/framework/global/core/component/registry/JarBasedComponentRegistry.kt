package kr.hqservice.framework.global.core.component.registry

import org.koin.core.component.KoinComponent
import java.io.File
import java.util.jar.JarFile
import java.util.logging.Level
import java.util.logging.Logger

abstract class JarBasedComponentRegistry : AbstractComponentRegistry(), KoinComponent {
    private val logger = Logger.getLogger(JarBasedComponentRegistry::class.java.name)

    /**
     * @return package to scan
     */
    abstract fun getComponentScope(): String

    abstract fun getJar(): File

    abstract fun getPluginClassLoader(): ClassLoader

    open fun filterComponent(clazz: Class<*>): Boolean {
        return true
    }

    final override fun getAllComponentsToScan(): Collection<Class<*>> {
        val classes: MutableSet<Class<*>> = mutableSetOf()
        val classLoader = getPluginClassLoader()
        JarFile(getJar()).use { jar ->
            val entries = jar.entries()
            while (entries.hasMoreElements()) {
                val name = entries.nextElement().name.replace("/", ".")
                if (name.startsWith(getComponentScope()) && name.endsWith(".class")) {
                    val className = name.removeSuffix(".class")
                    try {
                        val clazz = Class.forName(className, false, classLoader)
                        if (filterComponent(clazz)) {
                            classes.add(clazz)
                        }
                    } catch (exception: ClassNotFoundException) {
                        logSkippedClass(className, exception)
                    } catch (error: LinkageError) {
                        logSkippedClass(className, error)
                    }
                }
            }
        }
        return classes
    }

    private fun logSkippedClass(className: String, throwable: Throwable) {
        logger.log(Level.FINE, "skipping $className (${throwable::class.java.name})")
    }
}
