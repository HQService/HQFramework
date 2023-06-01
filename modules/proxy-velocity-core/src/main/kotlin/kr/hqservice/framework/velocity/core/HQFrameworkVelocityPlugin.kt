package kr.hqservice.framework.velocity.core

import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kr.hqservice.framework.global.core.HQPlugin
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

abstract class HQFrameworkVelocityPlugin : HQPlugin {
    final override fun getJar(): File {
        return findTargetPluginJar()
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
                        val json: Map<String, JsonElement> = Json.parseToJsonElement(pluginInfoReader.readText()).jsonObject
                        return json["main"]!!.jsonPrimitive.content
                    }
                }
            }
            throw NullPointerException("플러그인의 velocity-plugin.json 을 로드할 수 없습니다.")
        }
    }

    abstract fun getVelocityEventManager(): com.velocitypowered.api.event.EventManager

    abstract fun getProxyServer(): ProxyServer
}