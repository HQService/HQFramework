package kr.hqservice.framework.bukkit.core.netty

import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.event.NettyClientDisconnectedEvent
import kr.hqservice.framework.bukkit.core.netty.event.NettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import kr.hqservice.framework.netty.HQNettyBootstrap
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.message.BroadcastPacket
import kr.hqservice.framework.netty.packet.message.MessagePacket
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import kr.hqservice.framework.netty.pipeline.TimeOutHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class NettyClientBootstrap(
    private val plugin: Plugin,
    private val logger: Logger,
    private val config: HQYamlConfiguration
) {
    private companion object {
        const val RECONNECT_DELAY_TICKS = 60L
    }

    private var bootup = true
    private var currentBootstrap: HQNettyBootstrap? = null
    @Volatile
    private var shutdown = false

    fun initializing() {
        if (shutdown || !plugin.isEnabled) return

        // 이전 bootstrap 이 있으면 EventLoopGroup 을 먼저 닫기
        currentBootstrap?.shutdown()

        val bootstrap = HQNettyBootstrap(logger, config)
        currentBootstrap = bootstrap
        val future = bootstrap.initClient(bootup)
        if (bootup) {
            Direction.OUTBOUND.registerPacket(BroadcastPacket::class)
            Direction.OUTBOUND.registerPacket(MessagePacket::class)
        }

        bootup = false
        future.whenCompleteAsync { channel, throwable ->
            if (throwable != null) {
                logger.severe("failed to bootup successfully.")
                scheduleReconnect()
                return@whenCompleteAsync
            }

            val handlerBoss = channel.pipeline().get(BossHandler::class.java)

            handlerBoss.setDisconnectionHandler {
                if (!plugin.isEnabled || shutdown) return@setDisconnectionHandler
                it.setEnabled(false)
                try {
                    plugin.getScheduler().runTask {
                        plugin.server.pluginManager.callEvent(NettyClientDisconnectedEvent(it))
                    }
                } catch (_: IllegalPluginAccessException) {
                    return@setDisconnectionHandler
                }

                scheduleReconnect()
            }

            handlerBoss.setPacketPreprocessHandler { packet, wrapper ->
                if (!plugin.isEnabled) return@setPacketPreprocessHandler
                plugin.server.pluginManager.callEvent(AsyncNettyPacketReceivedEvent(wrapper, packet))
                plugin.getScheduler().runTask {
                    plugin.server.pluginManager.callEvent(NettyPacketReceivedEvent(wrapper, packet))
                }
            }

            logger.info("netty-client initialization success!")

            handlerBoss.connectionState = ConnectionState.HANDSHAKING
            channel.writeAndFlush(HandShakePacket(plugin.server.port))
            channel.pipeline().addFirst("timeout-handler", TimeOutHandler(5L, TimeUnit.SECONDS))
        }
    }

    private fun scheduleReconnect() {
        if (shutdown || !plugin.isEnabled) return
        try {
            plugin.getScheduler().runTaskLaterAsynchronously(RECONNECT_DELAY_TICKS) {
                if (!shutdown && plugin.isEnabled) initializing()
            }
        } catch (_: IllegalPluginAccessException) {
        }
    }

    fun shutdown() {
        shutdown = true
        currentBootstrap?.shutdown()
        currentBootstrap = null
    }
}
