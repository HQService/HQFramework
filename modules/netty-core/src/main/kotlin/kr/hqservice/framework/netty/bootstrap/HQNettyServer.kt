package kr.hqservice.framework.netty.bootstrap

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.CoroutineDispatcher
import kr.hqservice.framework.netty.HQChannelInitializer
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.channel.ChannelConnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelDisconnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelListPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Logger

class HQNettyServer(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val group: EventLoopGroup,
    private val blockingDispatcher: CoroutineDispatcher
) {
    private companion object {
        val defaultsRegistered = AtomicBoolean(false)
    }

    fun start(): CompletableFuture<Channel> {
        registerDefaults()

        val future = CompletableFuture<Channel>()
        val bootstrap = ServerBootstrap()
        bootstrap.channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark(1 shl 20, 2 shl 20))
            .childHandler(HQChannelInitializer(logger, blockingDispatcher, true, config.getString("netty.secret", "")))
            .localAddress(config.getString("netty.host", "127.0.0.1"), config.getInt("netty.port", 11286))
            .group(group)
            .bind()
            .addListener(ChannelFutureListener {
                if (it.isSuccess)
                    future.complete(it.channel())
                else future.completeExceptionally(it.cause())
            })
        return future
    }

    private fun registerDefaults() {
        if (defaultsRegistered.compareAndSet(false, true)) {
            Direction.INBOUND.registerPacket(RelayingPacket::class)
            Direction.OUTBOUND.registerPacket(ShutdownPacket::class)
            Direction.OUTBOUND.registerPacket(ChannelListPacket::class)
            Direction.OUTBOUND.registerPacket(ChannelConnectedPacket::class)
            Direction.OUTBOUND.registerPacket(ChannelDisconnectedPacket::class)
            Direction.OUTBOUND.registerPacket(PlayerConnectionPacket::class)
            Direction.INBOUND.addListener(PingPongPacket::class) { packet, channel ->
                channel.channel.writeAndFlush(PingPongPacket(packet.time, -1L))
            }
        }
    }

}