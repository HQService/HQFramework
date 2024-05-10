package kr.hqservice.framework.netty.packet.channel

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readChannels
import kr.hqservice.framework.netty.packet.extension.readPlayers
import kr.hqservice.framework.netty.packet.extension.writeChannels
import kr.hqservice.framework.netty.packet.extension.writePlayers

class ChannelListPacket(
    private var channels: MutableList<NettyChannel>,
    private var players: MutableList<NettyPlayer>
) : Packet() {
    fun getChannels(): List<NettyChannel> {
        return channels
    }

    fun getPlayers(): List<NettyPlayer> {
        return players
    }

    override fun write(buf: ByteBuf) {
        buf.writeChannels(channels)
        buf.writePlayers(players)
    }

    override fun read(buf: ByteBuf) {
        channels = buf.readChannels().toMutableList()
        players = buf.readPlayers().toMutableList()

    }
}