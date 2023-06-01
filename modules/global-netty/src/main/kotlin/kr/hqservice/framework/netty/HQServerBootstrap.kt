package kr.hqservice.framework.netty

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

class HQServerBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
) {
    private val group = NioEventLoopGroup(0, ThreadFactoryBuilder().setNameFormat("HQ IO Thread #%1\$d").build())

    //TODO("준형님이 할 거 AUTO_CONNECT , COROUTINE")

    fun initClient(isBootUp: Boolean): CompletableFuture<Channel> {
        if (isBootUp) {
            Direction.INBOUND.registerPacket(ShutdownPacket::class)
            Direction.INBOUND.registerPacket(RelayingPacket::class)
            Direction.INBOUND.addListener(PingPongPacket::class) { packet, channel ->
                if (packet.receivedTime == -1L) {
                    val newPacket = PingPongPacket(packet.time, System.currentTimeMillis())
                    newPacket.setCallbackResult(true)
                    channel.startCallback(newPacket, PingPongPacket::class) {
                        channel.pingCalculator.process(System.currentTimeMillis() - it.receivedTime)
                    }
                }
            }
        }

        val future = CompletableFuture<Channel>()
        val bootstrap = Bootstrap()
        bootstrap.channel(NioSocketChannel::class.java)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(HQChannelInitializer(logger))
            .group(group)
            .connect(InetSocketAddress(config.getString("netty.host"), config.getInt("netty.port")))
            .addListener(ChannelFutureListener {
                if (it.isSuccess)
                    future.complete(it.channel())
                else future.completeExceptionally(it.cause())
            })
        return future
    }

    fun initServer(): CompletableFuture<Channel> {
        Direction.OUTBOUND.registerPacket(ShutdownPacket::class)
        Direction.INBOUND.registerPacket(RelayingPacket::class)
        Direction.INBOUND.addListener(PingPongPacket::class) { packet, channel ->
            channel.channel.writeAndFlush(PingPongPacket(packet.time, -1L))
        }

        val future = CompletableFuture<Channel>()
        val bootstrap = ServerBootstrap()
        bootstrap.channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childHandler(HQChannelInitializer(logger, true))
            .localAddress(config.getString("netty.host"), config.getInt("netty.port"))
            .group(group)
            .bind()
            .addListener(ChannelFutureListener {
                if (it.isSuccess)
                    future.complete(it.channel())
                else future.completeExceptionally(it.cause())
            })
        return future
    }

}