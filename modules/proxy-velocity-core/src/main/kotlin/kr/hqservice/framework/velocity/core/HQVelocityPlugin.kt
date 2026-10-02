package kr.hqservice.framework.velocity.core

import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.proxy.core.HQProxyPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import java.nio.file.Files
import java.util.logging.Logger

abstract class HQVelocityPlugin : HQProxyPlugin, KoinComponent {
    protected open val velocityComponentRegistry: ComponentRegistry by inject { parametersOf(this) }

    abstract fun getEventManager(): EventManager
    abstract fun getProxyServer(): ProxyServer
    abstract fun getSlf4jLogger(): org.slf4j.Logger
    abstract fun getPluginContainer(): PluginContainer

    @Subscribe
    fun onProxyEnable(event: ProxyInitializeEvent) {
        proxyServer = getProxyServer()
        onLoad()
        onPreEnable()
        loadConfig()
        onEnable()
        onPostEnable()
    }

    @Subscribe
    fun onProxyDisable(event: ProxyShutdownEvent) {
        if (!disablesAfterOtherPlugins()) disablePlugin()
    }

    @Subscribe(order = PostOrder.LAST)
    fun onProxyDisableLast(event: ProxyShutdownEvent) {
        if (disablesAfterOtherPlugins()) disablePlugin()
    }

    protected open fun disablesAfterOtherPlugins(): Boolean = false

    private fun disablePlugin() {
        onPreDisable()
        onDisable()
        onPostDisable()
    }

    final override fun onLoad() {
        onPreLoad()
        onPostLoad()
    }

    final override fun onEnable() {
        velocityComponentRegistry.setup()
    }

    final override fun onDisable() {
        velocityComponentRegistry.teardown()
    }

    final override fun getComponentRegistry(): ComponentRegistry {
        return velocityComponentRegistry
    }

    final override fun getJar(): File {
        return getPluginContainer().description.source
            .orElseThrow { IllegalStateException("plugin ${getPluginContainer().description.id} has no source jar") }
            .toFile()
    }

    final override fun getLogger(): Logger {
        return Logger.getLogger(this.getSlf4jLogger().name)
    }

    final override fun getPluginClassLoader(): ClassLoader {
        return Thread.currentThread().contextClassLoader
    }

    private fun loadConfig() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs()
        if (File(getDataFolder(), "config.yml").exists()) return

        val config = getDataFolder().resolve("config.yml").toPath()
        this.javaClass.classLoader.getResourceAsStream("config.yml")?.use { stream ->
            Files.copy(stream, config)
        }
    }

    internal companion object {
        var proxyServer: ProxyServer? = null
    }
}