package kr.hqservice.framework.proxy.core.component.registry

import kr.hqservice.framework.global.core.component.registry.JarBasedComponentRegistry
import kr.hqservice.framework.proxy.core.HQProxyPlugin
import java.util.jar.JarFile

abstract class ProxyComponentRegistry<T : HQProxyPlugin>(private val plugin: T) : JarBasedComponentRegistry() {
    override fun getComponentScope(): String {
        return plugin::class.java.packageName
    }

    override fun getJar(): JarFile {
        return JarFile(plugin.getJar())
    }
}