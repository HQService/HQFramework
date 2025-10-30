package kr.hqservice.framework.velocity.core.netty

import io.netty.channel.ChannelId
import io.netty.util.concurrent.ScheduledFuture
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.netty.HQNettyBootstrap
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.message.BroadcastPacket
import kr.hqservice.framework.netty.packet.message.MessagePacket
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.packet.server.RelayingResult
import kr.hqservice.framework.netty.pipeline.ConnectionState
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

@Bean
class NettyServerBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val channelRegistry: NettyChannelRegistry,
    private val packetSender: PacketSender
) : KoinComponent {
    private val hbStates = ConcurrentHashMap<ChannelId, HbState>()
    private val hbTasks = ConcurrentHashMap<ChannelId, ScheduledFuture<*>>()

    private data class HbState(
        var awaiting: Boolean = false,
        var lastSendNano: Long = 0L,
        var missed: Int = 0
    )

    fun initializing() {
        val future = HQNettyBootstrap(logger, config).initServer()
        future.whenCompleteAsync { _, throwable ->
            if (throwable != null) {
                logger.severe("failed to bootup successfully.")
                throwable.printStackTrace()
            } else logger.info("server initialization success!")
        }

        Direction.INBOUND.registerPacket(BroadcastPacket::class)
        Direction.INBOUND.registerPacket(MessagePacket::class)
        registerDefaultListeners()
    }

    fun shutdown() {
        channelRegistry.shutdown()
    }

    private fun registerDefaultListeners() {
        Direction.INBOUND.addListener(PingPongPacket::class) { pkt, wrapper ->
            val state = hbStates[wrapper.channel.id()] ?: return@addListener
            state.awaiting = false
            state.missed = 0
            wrapper.channel.writeAndFlush(PingPongPacket(pkt.time, -1L))
        }

        Direction.INBOUND.addListener(HandShakePacket::class) { packet, wrapper ->
            wrapper.port = packet.port
            wrapper.handler.connectionState = ConnectionState.CONNECTED
            wrapper.channel.writeAndFlush(HandShakePacket(-1))
            channelRegistry.registerActiveChannel(packet.port, wrapper)
            logger.info("registered channel ${channelRegistry.getChannelNameByPort(packet.port)}")
            val ch = wrapper.channel
            val chId = ch.id()
            val state = HbState()
            hbStates[chId] = state

            val intervalMs = 1000L
            val timeoutMs = 3000L
            val maxMissed = 3

            val task = ch.eventLoop().scheduleAtFixedRate({
                if (!ch.isActive || wrapper.handler.connectionState != ConnectionState.CONNECTED) {
                    return@scheduleAtFixedRate
                }
                val now = System.nanoTime()
                if (state.awaiting) {
                    val waitedMs = (now - state.lastSendNano) / 1_000_000
                    if (waitedMs >= timeoutMs) {
                        state.missed += 1
                        state.awaiting = false
                        if (state.missed >= maxMissed) {
                            ch.close()
                            return@scheduleAtFixedRate
                        }
                    } else return@scheduleAtFixedRate
                }

                state.awaiting = true
                state.lastSendNano = now
                ch.writeAndFlush(PingPongPacket(-1L, System.currentTimeMillis()))
            }, intervalMs, intervalMs, TimeUnit.MILLISECONDS)

            hbTasks[chId] = task
            ch.closeFuture().addListener {
                hbTasks.remove(chId)?.cancel(false)
                hbStates.remove(chId)
            }
        }

        Direction.INBOUND.addListener(RelayingPacket::class) { packet, _ ->
            try {
                try {
                    val port = packet.targetServer.toInt()
                    if (port == -1) {
                        channelRegistry.forEachChannels { it.channel.writeAndFlush(RelayingResult(packet.getRelayByte())) }
                        return@addListener
                    } else channelRegistry.getChannelByPort(port)
                } catch (e: NumberFormatException) {
                    channelRegistry.getChannelByServerName(packet.targetServer)
                }.channel.writeAndFlush(RelayingResult(packet.getRelayByte()))
            } catch (e: IllegalArgumentException) {
                logger.severe("Relaying packet failed due to TargetServer Offline!")
            }
        }

        Direction.INBOUND.addListener(BroadcastPacket::class) { packet, _ ->
            val targetChannel = packet.targetChannel
            if (targetChannel != null) {
                packetSender.sendMessageToChannel(targetChannel, packet.message, packet.logging)
            } else packetSender.broadcast(packet.message, packet.logging)
        }

        Direction.INBOUND.addListener(MessagePacket::class) { packet, _ ->
            packetSender.sendMessageToPlayers(packet.receivers, packet.message, packet.logging)
        }
    }
}