package kr.hqservice.framework.netty

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import kr.hqservice.framework.netty.bootstrap.HQNettyClient
import kr.hqservice.framework.netty.bootstrap.HQNettyServer
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.bytebuddy.agent.ByteBuddyAgent
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

class HQNettyBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
) {
    private val group = NioEventLoopGroup(0, ThreadFactoryBuilder().setNameFormat("HQ IO Thread #%1\$d").build())

    private fun init() {
        ByteBuddyAgent.install()

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