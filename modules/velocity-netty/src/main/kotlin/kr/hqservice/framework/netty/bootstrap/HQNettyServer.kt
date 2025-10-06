package kr.hqservice.framework.netty.bootstrap

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
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
import java.util.logging.Logger

class HQNettyServer(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val group: EventLoopGroup,
    private val workerGroup: EventLoopGroup
) {
    fun start(): CompletableFuture<Channel> {
        Direction.INBOUND.registerPacket(RelayingPacket::class)
        Direction.OUTBOUND.registerPacket(ShutdownPacket::class)
        Direction.OUTBOUND.registerPacket(ChannelListPacket::class)
        Direction.OUTBOUND.registerPacket(ChannelConnectedPacket::class)
        Direction.OUTBOUND.registerPacket(ChannelDisconnectedPacket::class)
        Direction.OUTBOUND.registerPacket(PlayerConnectionPacket::class)
        Direction.INBOUND.addListener(PingPongPacket::class) { packet, channel ->
            channel.channel.writeAndFlush(PingPongPacket(packet.time, -1L))
        }

        val future = CompletableFuture<Channel>()
        val bootstrap = ServerBootstrap()
        bootstrap
            .group(group, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childHandler(HQChannelInitializer(logger, true))
            .localAddress(config.getString("netty.host"), config.getInt("netty.port"))
            .bind()
            .addListener(ChannelFutureListener {
                if (it.isSuccess)
                    future.complete(it.channel())
                else future.completeExceptionally(it.cause())
            })
        return future
    }
}