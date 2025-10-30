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
    private val totalThread = config.getInt("netty.thread").run {
        if (this < 1) Runtime.getRuntime().availableProcessors() * 2 else this
    }.let {
        val capped = max(1, min(Runtime.getRuntime().availableProcessors() * 4, it))
        logger.info("Netty Total Thread Count: $capped")
        capped
    }

    private val bossThread = if (totalThread >= 8) 2 else 1
    private val workerThread = max(1, totalThread - bossThread).also {
        logger.info("Netty Boss Thread Count: $bossThread, Worker Thread Count: $it")
    }

    private val bossGroup: EventLoopGroup = when {
        Epoll.isAvailable() -> EpollEventLoopGroup(bossThread, ThreadFactoryBuilder().setNameFormat("HQ-epoll-boss-%d").build())
        KQueue.isAvailable() -> KQueueEventLoopGroup(bossThread, ThreadFactoryBuilder().setNameFormat("HQ-kqueue-boss-%d").build())
        else -> NioEventLoopGroup(bossThread, ThreadFactoryBuilder().setNameFormat("HQ-nio-boss-%d").build())
    }

    private val workerGroup: EventLoopGroup = when {
        Epoll.isAvailable() -> EpollEventLoopGroup(workerThread, ThreadFactoryBuilder().setNameFormat("HQ-epoll-worker-%d").build())
        KQueue.isAvailable() -> KQueueEventLoopGroup(workerThread, ThreadFactoryBuilder().setNameFormat("HQ-kqueue-worker-%d").build())
        else -> NioEventLoopGroup(workerThread, ThreadFactoryBuilder().setNameFormat("HQ-nio-worker-%d").build())
    }

    private fun init() {
        Direction.INBOUND.registerPacket(HandShakePacket::class)
        Direction.OUTBOUND.registerPacket(HandShakePacket::class)
        Direction.INBOUND.registerPacket(PingPongPacket::class)
        Direction.OUTBOUND.registerPacket(PingPongPacket::class)
    }

    fun initClient(isBootUp: Boolean): CompletableFuture<Channel> {
        if (isBootUp) init()
        return HQNettyClient(logger, config, bossGroup).start(isBootUp)
    }

    fun initServer(): CompletableFuture<Channel> {
        init()
        return HQNettyServer(logger, config, bossGroup, workerGroup).start()
    }

}