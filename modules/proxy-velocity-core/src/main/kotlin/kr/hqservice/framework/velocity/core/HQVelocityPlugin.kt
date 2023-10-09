package kr.hqservice.framework.velocity.core

import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kr.hqservice.framework.global.core.component.registry.ComponentRegistry
import kr.hqservice.framework.proxy.core.HQProxyPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.logging.Logger

abstract class HQVelocityPlugin : HQProxyPlugin, KoinComponent {
    protected open val velocityComponentRegistry: ComponentRegistry by inject { parametersOf(this) }

    abstract fun getEventManager(): EventManager
    abstract fun getProxyServer(): ProxyServer
    abstract fun getSlf4jLogger(): org.slf4j.Logger
    abstract fun getPluginContainer(): PluginContainer

    @Subscribe
    fun onProxyEnable(event: ProxyInitializeEvent) {
        onPreEnable()
        onEnable()
        onPostEnable()
    }

    @Subscribe
    fun onProxyDisable(event: ProxyShutdownEvent) {
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
        return findTargetPluginJar()
    }

    final override fun getLogger(): Logger {
        return Logger.getLogger(this.getSlf4jLogger().name)
    }

    final override fun getPluginClassLoader(): ClassLoader {
        return Thread.currentThread().contextClassLoader
    }

    private fun findTargetPluginJar(): File {
        val pluginPath = Path.of("plugins")
        val pluginJarPath = Files.newDirectoryStream(pluginPath) { path ->
            path.toFile().isFile && path.toString().endsWith(".jar")
        }.first { path -> this::class.qualifiedName == getPluginMainClass(path) }
        return pluginJarPath.toFile()
    }

    private fun getPluginMainClass(source: Path): String {
        JarInputStream(BufferedInputStream(Files.newInputStream(source))).use { `in` ->
            var entry: JarEntry
            while (`in`.nextJarEntry.also { entry = it } != null) {
                if (entry.name == "velocity-plugin.json") {
                    InputStreamReader(`in`, StandardCharsets.UTF_8).use { pluginInfoReader ->
                        val json: Map<String, JsonElement> =
                            Json.parseToJsonElement(pluginInfoReader.readText()).jsonObject
                        return json["main"]!!.jsonPrimitive.content
                    }
                }
            }
            throw NullPointerException("플러그인의 velocity-plugin.json 을 로드할 수 없습니다.")
        }
    }
}