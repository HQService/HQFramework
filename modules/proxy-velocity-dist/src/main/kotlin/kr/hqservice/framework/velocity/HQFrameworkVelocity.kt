package kr.hqservice.framework.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.nio.file.Path
import java.util.logging.Logger

@Plugin(
    id = "hqframework",
    name = "HQFramework",
    description = "framework for proxies",
    version = "1.0.0",
    url = "https://github.com/HighQualityService/HQFramework",
    authors = ["vjh0107", "cccgh5"]
)
class HQFrameworkVelocity @Inject constructor(
    private val server: ProxyServer,
    private val container: PluginContainer,
    private val logger: org.slf4j.Logger,
    private val eventManager: EventManager,
    @DataDirectory private val dataFolder: Path
) : HQFrameworkVelocityPlugin() {
    override fun getDataFolder(): File {
        return dataFolder.toFile()
    }

    override fun getSlf4jLogger(): org.slf4j.Logger {
        return logger
    }

    override fun getProxyServer(): ProxyServer {
        return server
    }

    override fun getEventManager(): EventManager {
        return eventManager
    }

    override fun getPluginContainer(): PluginContainer {
        return container
    }
}