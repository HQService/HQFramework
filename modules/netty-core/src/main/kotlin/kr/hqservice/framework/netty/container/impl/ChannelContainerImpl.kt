package kr.hqservice.framework.netty.container.impl

import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.container.ChannelContainer
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Bean
@Singleton(binds = [ChannelContainer::class])
class ChannelContainerImpl : ChannelContainer {
    private val lock = ReentrantLock()
    private val channels = mutableMapOf<Int, NettyChannel>()
    private val players = mutableMapOf<UUID, NettyPlayer>()

    fun registerChannel(channel: NettyChannel) = lock.withLock {
        channels[channel.getPort()] = channel
    }

    fun unregisterChannel(channel: NettyChannel) = lock.withLock {
        channels.remove(channel.getPort())
        val port = channel.getPort()
        players.entries.removeAll { it.value.getChannel()?.getPort() == port }
    }

    fun addPlayer(player: NettyPlayer) = lock.withLock {
        players[player.getUniqueId()] = player
        player.getChannel()?.let { ch ->
            channels.putIfAbsent(ch.getPort(), ch)
        }
    }

    fun removePlayer(player: NettyPlayer) = lock.withLock {
        players.remove(player.getUniqueId())
    }

    fun resetState(allChannels: List<NettyChannel>, allPlayers: List<NettyPlayer>) = lock.withLock {
        channels.clear()
        players.clear()
        allChannels.forEach { channels[it.getPort()] = it }
        allPlayers.forEach {
            players[it.getUniqueId()] = it
            it.getChannel()?.let { ch -> channels.putIfAbsent(ch.getPort(), ch) }
        }
    }

    override fun getChannels(): List<NettyChannel> = lock.withLock {
        channels.values.toList()
    }

    override fun getPlayers(): List<NettyPlayer> = lock.withLock {
        players.values.toList()
    }

    override fun getPlayers(channel: NettyChannel): List<NettyPlayer> = lock.withLock {
        val port = channel.getPort()
        players.values.filter { it.getChannel()?.getPort() == port }
    }
}
