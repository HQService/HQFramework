package kr.hqservice.framework.velocity.core.netty.listener

import com.google.gson.Gson
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
import com.imaginarycode.minecraft.redisbungee.api.events.IPlayerChangedServerNetworkEvent
import com.imaginarycode.minecraft.redisbungee.api.events.IPlayerJoinedNetworkEvent
import com.imaginarycode.minecraft.redisbungee.api.events.IPlayerLeftNetworkEvent
import com.imaginarycode.minecraft.redisbungee.api.events.IPubSubMessageEvent
import com.velocitypowered.api.event.Subscribe
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import java.util.UUID

class PlayerConnectionListener(
    private val channelContainer: NettyChannelRegistry
) {
    private val absRedis = RedisBungeeAPI.getAbstractRedisBungeeAPI()
    private val gson = Gson()
    data class PlayerSwitchData(
        val proxyName: String,
        val from: String?,
        val to: String,
        private val uuid: String
    ) { fun getUniqueId() = UUID.fromString(uuid) }

    @Subscribe
    fun onCatchJoin(event: IPlayerJoinedNetworkEvent) {
        val nettyPlayer = NettyPlayerImpl(
            absRedis.getNameFromUuid(event.uuid),
            absRedis.getNameFromUuid(event.uuid),
            event.uuid,
            null
        )

        val packet = PlayerConnectionPacket(
            nettyPlayer,
            PlayerConnectionState.PRE_CONNECT,
            null
        )

        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @Subscribe
    fun test(event: IPubSubMessageEvent) {
        if (event.channel == "redisbungee-serverchange") {
            val switchData = gson.fromJson(event.message, PlayerSwitchData::class.java)
            val uniqueId = switchData.getUniqueId()
            val to = getChannelByName(switchData.to)
            val playerName = absRedis.getNameFromUuid(uniqueId)

            if (switchData.from == null || switchData.from == "null") {
                val nettyPlayer = NettyPlayerImpl(playerName, playerName, uniqueId, to)
                val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.CONNECTED, to)
                channelContainer.loopChannels { it.sendPacket(packet) }
            } else {
                val from = getChannelByName(switchData.from)

                if (to?.getName() == from?.getName()) return
                val nettyPlayer = NettyPlayerImpl(playerName, playerName, uniqueId, from)
                val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.PRE_SWITCH_CHANNEL, to)
                channelContainer.loopChannels { it.sendPacket(packet) }
            }
        }
    }

    @Subscribe
    fun playerPreLoginEvent(event: IPlayerChangedServerNetworkEvent) {
        event.previousServer ?: return
        val from = getChannelByName(event.previousServer) ?: return
        val to = getChannelByName(event.server) ?: return
        val playerName = absRedis.getNameFromUuid(event.uuid)

        if (from.getName() == to.getName()) return
        val nettyPlayer = NettyPlayerImpl(playerName, playerName, event.uuid, to)
        val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.SWITCHED_CHANNEL, from)
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    @Subscribe
    fun playerDisconnectEvent(event: IPlayerLeftNetworkEvent) {
        val playerName = absRedis.getNameFromUuid(event.uuid)
        val channel = absRedis.getServerNameFor(event.uuid)?.let { getChannelByName(it) }
        val nettyPlayer = NettyPlayerImpl(playerName, playerName, event.uuid, channel)
        val packet = PlayerConnectionPacket(nettyPlayer, PlayerConnectionState.DISCONNECT, channel)
        channelContainer.loopChannels { it.sendPacket(packet) }
    }

    private fun getChannelByName(channelName: String): NettyChannel? {
        return try {
            val port = channelContainer.getChannelByServerName(channelName)
            NettyChannelImpl(port.port, channelName)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}