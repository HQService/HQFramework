package kr.hqservice.framework.global.core.component.registry

import org.koin.core.component.KoinComponent
import java.util.jar.JarFile

abstract class JarBasedComponentRegistry : AbstractComponentRegistry(), KoinComponent {

    /**
     * @return package to scan
     */
    abstract fun getComponentScope(): String

    abstract fun getJar(): JarFile

    open fun filterComponent(clazz: Class<*>): Boolean {
        return true
    }

    final override fun getAllComponentsToScan(): Collection<Class<*>> {
        val classes: MutableSet<Class<*>> = mutableSetOf()
        val entries = getJar().entries()
        while (entries.hasMoreElements()) {
            val name = entries.nextElement().name.replace("/", ".")
            if (name.startsWith(getComponentScope()) && name.endsWith(".class")) {
                try {
                    val clazz = Class.forName(name.removeSuffix(".class"))
                    if (filterComponent(clazz)) {
                        classes.add(clazz)
                    }
                } catch (exception: ClassNotFoundException) {
                    exception.printStackTrace()
                }
            }
        }
        return classes
    }
}