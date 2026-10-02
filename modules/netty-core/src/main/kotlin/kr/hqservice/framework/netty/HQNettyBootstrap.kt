package kr.hqservice.framework.netty

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import kotlinx.coroutines.asCoroutineDispatcher
import kr.hqservice.framework.netty.bootstrap.HQNettyClient
import kr.hqservice.framework.netty.bootstrap.HQNettyServer
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
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
    private val group: EventLoopGroup =
        NioEventLoopGroup(ioThreads, ThreadFactoryBuilder().setNameFormat("HQ-nio-%d").build())
    private val blockingGroup = DefaultEventExecutorGroup(
        Runtime.getRuntime().availableProcessors(),
        ThreadFactoryBuilder().setNameFormat("HQ-blocking-%d").setDaemon(true).build()
    )
    private val blockingDispatcher = blockingGroup.asCoroutineDispatcher()

    private fun init() {
        Direction.INBOUND.registerPacket(HandShakePacket::class)
        Direction.OUTBOUND.registerPacket(HandShakePacket::class)
        Direction.INBOUND.registerPacket(PingPongPacket::class)
        Direction.OUTBOUND.registerPacket(PingPongPacket::class)
    }

    fun initClient(isBootUp: Boolean): CompletableFuture<Channel> {
        if (isBootUp) init()
        return HQNettyClient(logger, config, group, blockingDispatcher).start()
    }

    fun initServer(): CompletableFuture<Channel> {
        init()
        if (config.getString("netty.secret", "").isBlank()) {
            logger.warning("netty.secret is empty; any client that can reach the port can register as a backend")
        }
        return HQNettyServer(logger, config, group, blockingDispatcher).start()
    }

    fun shutdown() {
        shutdownIfRunning(blockingGroup)
        shutdownIfRunning(group)
    }

    private fun shutdownIfRunning(executorGroup: EventExecutorGroup) {
        if (!executorGroup.isShuttingDown && !executorGroup.isShutdown) {
            executorGroup.shutdownGracefully(0, 2, TimeUnit.SECONDS)
        }
    }

}