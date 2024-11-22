package kr.hqservice.framework.velocity.core.module

import com.google.gson.Gson
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.component.module.Module
import kr.hqservice.framework.velocity.core.component.module.Setup
import kr.hqservice.framework.velocity.core.component.module.Teardown
import kr.hqservice.framework.velocity.core.netty.listener.PlayerLastConnectionListener
import kr.hqservice.framework.velocity.core.registry.PlayerLastConnectionRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.io.File

@Module
class PlayerLastConnectionModule(
    private val plugin: HQVelocityPlugin,
    config: HQYamlConfiguration,
    private val playerLastConnectionRegistry: PlayerLastConnectionRegistry
) {
    private val isLastConnectionEnabled = config.getBoolean("last-connection")

    private val gson by lazy { Gson() }
    private val file by lazy { File("last-connection.json") }

    @Setup
    fun setup() {
        if (isLastConnectionEnabled) {
            loadPlayerLastConnection()
            val proxyServer = plugin.getProxyServer()
            val playerLastConnectionListener = PlayerLastConnectionListener(proxyServer, playerLastConnectionRegistry)
            plugin.getEventManager().register(plugin, playerLastConnectionListener)
        }
    }

    private fun loadPlayerLastConnection() {
        if (!file.exists()) return
        val playerLastConnectionRegistry = gson.fromJson(
            file.readLines().joinToString(),
            PlayerLastConnectionRegistry::class.java
        )
        this.playerLastConnectionRegistry.restore(playerLastConnectionRegistry)
    }

    @Teardown
    fun teardown() {
        if (isLastConnectionEnabled) {
            savePlayerLastConnection()
        }
    }

    private fun savePlayerLastConnection() {
        val json = gson.toJson(playerLastConnectionRegistry)
        file.writeText(json)
    }
}