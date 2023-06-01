package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.yaml.extension.yaml
import net.md_5.bungee.api.plugin.Plugin
import org.koin.core.component.KoinComponent
import java.io.File
import java.util.logging.Logger

@Component
class NettyModule(
    plugin: Plugin,
    logger: Logger,
) : HQModule, KoinComponent {
    private val config = File(plugin.dataFolder, "config.yml").yaml()
    private val nettyEnabled: Boolean = config.getBoolean("netty.enabled")
    private val bootstrap = NettyServerBootstrap(logger, config)

    override fun onEnable() {
        if(nettyEnabled) bootstrap.initializing()
    }

    override fun onDisable() {
        bootstrap.shutdown()
    }
}