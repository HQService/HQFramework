package kr.hqservice.framework.netty.bootstrap

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import kr.hqservice.framework.netty.HQChannelInitializer
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.container.ChannelContainer
import kr.hqservice.framework.netty.container.impl.ChannelContainerImpl
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.channel.ChannelConnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelDisconnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelListPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.packet.server.RelayingPacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

class HQNettyClient(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val group: EventLoopGroup
) : KoinComponent {
    private val container: ChannelContainer by inject()

    fun start(isBootUp: Boolean): CompletableFuture<Channel> {
        if (isBootUp) {
            Direction.OUTBOUND.registerPacket(RelayingPacket::class)
            Direction.INBOUND.registerPacket(ShutdownPacket::class)
            Direction.INBOUND.registerPacket(ChannelListPacket::class)
            Direction.INBOUND.registerPacket(ChannelConnectedPacket::class)
            Direction.INBOUND.registerPacket(ChannelDisconnectedPacket::class)
            Direction.INBOUND.registerPacket(PlayerConnectionPacket::class)
            Direction.INBOUND.addListener(PingPongPacket::class) { packet, channel ->
                if (packet.receivedTime == -1L) {
                    val newPacket = PingPongPacket(packet.time, System.currentTimeMillis())
                    newPacket.setCallbackResult(true)
                    channel.startCallback(newPacket, PingPongPacket::class) {
                        channel.pingCalculator.process(System.currentTimeMillis() - it.receivedTime)
                    }
                }
            }

            val container = container as ChannelContainerImpl
            Direction.INBOUND.addListener(ChannelConnectedPacket::class) { packet, _ ->
                container.registerChannel(packet.channel)
            }

            Direction.INBOUND.addListener(ChannelDisconnectedPacket::class) { packet, _ ->
                container.unregisterChannel(packet.channel)
            }

            Direction.INBOUND.addListener(ChannelListPacket::class) { packet, _ ->
                /*println("ChannelListPacket Received Log -> ")
                println("channels: ${packet.getChannels().map { it.getName() + "-" + it.getPort() }}")
                println("players: ${packet.getPlayers().map { it.getName() + "-" + it.getChannel()?.getPort() }}")*/
                packet.getChannels().forEach(container::registerChannel)
                packet.getPlayers().forEach(container::addPlayer)
            }

            Direction.INBOUND.addListener(PlayerConnectionPacket::class) { packet, _ ->
                /*println("PlayerConnectionPacket Received Log -> ")
                println("packet state: " + packet.state.name)
                println("packet source: (")
                println("   player: ")
                println("       name: ${packet.player.getName()}")
                println("       uuid: ${packet.player.getUniqueId()}")
                print("       channel: ")
                packet.player.getChannel().printLog(true)
                print("   channel: ")
                packet.sourceChannel.printLog(false)
                println(")")*/
                when (packet.state) {
                    PlayerConnectionState.CONNECTED,
                    PlayerConnectionState.SWITCHED_CHANNEL ->
                        container.addPlayer(packet.player)

                    PlayerConnectionState.DISCONNECT ->
                        container.removePlayer(packet.player)

                    else -> {}
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
                if (it.isSuccess) {
                    future.complete(it.channel())
                } else future.completeExceptionally(it.cause())
            })
        return future
    }

    private fun NettyChannel?.printLog(tab: Boolean) {
        if (this == null) print("null\n")
        else {
            val first = if (tab) "\t" else ""
            print("\n")
            println("$first\tname: ${getName()}")
            println("$first\tport: ${getPort()}")
        }
    }

}