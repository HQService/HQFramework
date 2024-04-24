package kr.hqservice.framework.velocity.core.netty.listener

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import java.net.InetSocketAddress
import java.util.*

class PlayerConnectionListener(
    private val channelContainer: NettyChannelRegistry
) {

    @Subscribe
    fun playerPreLoginEvent(event: PreLoginEvent) {
        val nettyPlayer = NettyPlayerImpl(
            event.username,
            event.username,
            UUID.randomUUID(),
            null
        )

        val packet = PlayerConnectionPacket(
            nettyPlayer,
            PlayerConnectionState.PRE_CONNECT,
            null
        )

        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @Subscribe(order = PostOrder.LAST)
    fun playerServerPreSwitchEvent(event: ServerPreConnectEvent) {
        val from = event.previousServer ?: return

        if (event.originalServer.serverInfo.address == from.serverInfo.address) return
        val nettyPlayer = NettyPlayerImpl(
            event.player.username,
            event.player.username,
            event.player.uniqueId,
            getChannelByAddress(from.serverInfo.address)
        )
        val packet = PlayerConnectionPacket(
            nettyPlayer,
            PlayerConnectionState.PRE_SWITCH_CHANNEL,
            getChannelByAddress(event.originalServer.serverInfo.address)
        )
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @Subscribe
    fun playerConnectedEvent(event: ServerConnectedEvent) {
        if (event.previousServer != null
            && event.previousServer.isPresent) return

        val nettyPlayer = NettyPlayerImpl(
            event.player.username,
            event.player.username,
            event.player.uniqueId,
            getChannelByAddress(event.server.serverInfo.address),
        )

        val packet = PlayerConnectionPacket(
            nettyPlayer,
            PlayerConnectionState.CONNECTED,
            getChannelByAddress(event.server.serverInfo.address)
        )

        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @Subscribe
    fun playerSwitchChannelEvent(event: ServerConnectedEvent) {
        val from = event.previousServer ?: return
        if (!from.isPresent) return

        if (event.server.serverInfo.address == from.get().serverInfo.address) return
        val nettyPlayer = NettyPlayerImpl(
            event.player.username,
            event.player.username,
            event.player.uniqueId,
            getChannelByAddress(event.server.serverInfo.address)
        )
        val packet = PlayerConnectionPacket(
            nettyPlayer,
            PlayerConnectionState.SWITCHED_CHANNEL,
            getChannelByAddress(from.get().serverInfo.address)
        )

        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @Subscribe
    fun playerDisconnectEvent(event: DisconnectEvent) {

        val nettyPlayer = NettyPlayerImpl(
            event.player.username,
            event.player.username,
            event.player.uniqueId,
            event.player.currentServer.run {
                if (isPresent) get()
                else null
            }?.serverInfo?.run { getChannelByAddress(this.address) }
        )

        val packet = PlayerConnectionPacket(
            nettyPlayer,
            PlayerConnectionState.DISCONNECT,
            event.player.currentServer.run {
                if (isPresent) get()
                else null
            }?.serverInfo?.run { getChannelByAddress(this.address) }
        )
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    private fun getChannelByAddress(address: InetSocketAddress): NettyChannel? {
        return try {
            val port = address.port
            val name = channelContainer.getChannelNameByPort(port)
            NettyChannelImpl(port, name)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun getChannelByPort(port: Int): NettyChannel? {
        return try {
            val name = channelContainer.getChannelNameByPort(port)
            NettyChannelImpl(port, name)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}