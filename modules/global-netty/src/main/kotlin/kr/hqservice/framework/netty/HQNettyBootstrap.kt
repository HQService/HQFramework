package kr.hqservice.framework.netty

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import kr.hqservice.framework.netty.bootstrap.HQNettyClient
import kr.hqservice.framework.netty.bootstrap.HQNettyServer
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.min

class HQNettyBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
) {
    private val ioThreads = config.getInt("netty.thread").run {
        if (this < 1) 2 else this
    }.let {
        max(1, min(Runtime.getRuntime().availableProcessors(), it))
    }
    private val group: EventLoopGroup = when {
        Epoll.isAvailable() -> EpollEventLoopGroup(ioThreads, ThreadFactoryBuilder().setNameFormat("HQ-epoll-%d").build())
        KQueue.isAvailable() -> KQueueEventLoopGroup(ioThreads, ThreadFactoryBuilder().setNameFormat("HQ-kqueue-%d").build())
        else -> NioEventLoopGroup(ioThreads, ThreadFactoryBuilder().setNameFormat("HQ-nio-%d").build())
    }

    private fun init() {
        Direction.INBOUND.registerPacket(HandShakePacket::class)
        Direction.OUTBOUND.registerPacket(HandShakePacket::class)
        Direction.INBOUND.registerPacket(PingPongPacket::class)
        Direction.OUTBOUND.registerPacket(PingPongPacket::class)
    }

    fun initClient(isBootUp: Boolean): CompletableFuture<Channel> {
        if (isBootUp) init()
        return HQNettyClient(logger, config, group).start(isBootUp)
    }

    fun initServer(): CompletableFuture<Channel> {
        init()
        return HQNettyServer(logger, config, group).start()
    }

}