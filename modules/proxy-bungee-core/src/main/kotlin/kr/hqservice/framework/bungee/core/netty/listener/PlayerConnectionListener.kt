package kr.hqservice.framework.bungee.core.netty.listener

import kr.hqservice.framework.bungee.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress
import java.util.*

class PlayerConnectionListener(
    private val channelContainer: NettyChannelRegistry
) : Listener {

    @EventHandler
    fun playerPreLoginEvent(event: PreLoginEvent) {
        val nettyPlayer = NettyPlayerImpl(
            event.connection.name,
            event.connection.uniqueId?: UUID.randomUUID(),
            null
        )
        val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.PRE_CONNECT, getChannelByAddress(event.connection.address))
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @EventHandler
    fun playerServerPreSwitchEvent(event: ServerConnectEvent) {
        val from = event.player.server?: return
        if(event.target.address == from.address) return
        val nettyPlayer = NettyPlayerImpl(
            event.player.name,
            event.player.uniqueId,
            getChannelByAddress(from.address)
        )
        val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.PRE_SWITCH_CHANNEL, getChannelByAddress(event.target.address))
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @EventHandler
    fun playerConnectedEvent(event: ServerConnectedEvent) {
        if(event.player.server != null) return
        val nettyPlayer = NettyPlayerImpl(
            event.player.name,
            event.player.uniqueId,
            null,
        )
        val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.CONNECTED, getChannelByAddress(event.server.address))
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @EventHandler
    fun playerSwitchChannelEvent(event: ServerSwitchEvent) {
        val from = event.from?: return
        if(event.player.server.address == from.address) return
        val nettyPlayer = NettyPlayerImpl(
            event.player.name,
            event.player.uniqueId,
            getChannelByAddress(event.player.server.address)
        )
        val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.SWITCHED_CHANNEL, getChannelByAddress(from.address))
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @EventHandler
    fun playerDisconnectEvent(event: PlayerDisconnectEvent) {
        val nettyPlayer = NettyPlayerImpl(
            event.player.name,
            event.player.uniqueId,
            getChannelByAddress(event.player.server.address)
        )
        val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.DISCONNECT, getChannelByAddress(event.player.pendingConnection.address))
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    private fun getChannelByAddress(address: InetSocketAddress): NettyChannel? {
        return try {
            val port = address.port
            val name = channelContainer.getChannelNameByPort(port)
            NettyChannelImpl(port, name)
        } catch (e: IllegalArgumentException) { null }
    }

}