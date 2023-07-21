package kr.hqservice.framework.netty.container.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.container.ChannelContainer

@Component
@Singleton(binds = [ChannelContainer::class])
class ChannelContainerImpl : ChannelContainer, HQSimpleComponent {
    private val channelMap = mutableMapOf<NettyChannel, MutableList<NettyPlayer>>()

    fun registerChannel(channel: NettyChannel) {
        channelMap.computeIfAbsent(channel) { mutableListOf() }
    }

    fun unregisterChannel(channel: NettyChannel) {
        channelMap.remove(channel)
    }

    fun addPlayer(player: NettyPlayer) {
        removePlayer(player)
        player.getChannel()?.apply { channelMap[this]?.add(player) }
    }

    fun removePlayer(player: NettyPlayer) {
        channelMap.values.forEach {
            it.removeIf { value -> value.getName() == player.getName() }
        }
    }

    override fun getChannels(): List<NettyChannel> {
        return channelMap.keys.toList()
    }

    override fun getPlayers(): List<NettyPlayer> {
        return channelMap.values.flatten()
    }

    override fun getPlayers(channel: NettyChannel): List<NettyPlayer> {
        return channelMap[channel] ?: throw IllegalArgumentException("unknown channel ${channel.getName()}")
    }
}