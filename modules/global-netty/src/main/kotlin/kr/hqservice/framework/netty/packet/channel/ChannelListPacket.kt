package kr.hqservice.framework.netty.packet.channel

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readChannels
import kr.hqservice.framework.netty.packet.extension.readVarInt
import kr.hqservice.framework.netty.packet.extension.writeChannels
import kr.hqservice.framework.netty.packet.extension.writeVarInt
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

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
        if(players.isEmpty()) buf.writeBytes(ByteArray(0))
        else ByteArrayOutputStream().use {
            ObjectOutputStream(it).use { oos ->
                oos.writeInt(players.size)
                players.forEach { player ->
                    oos.writeUTF(player.getName())
                    oos.writeUTF(player.getUniqueId().toString())
                    oos.writeInt(player.getChannel()?.getPort()?: -1)
                }
            }
            val byteArray = it.toByteArray().compress()
            buf.writeBytes(byteArray)
        }
    }

    override fun read(buf: ByteBuf) {
        channels = mutableListOf()
        players = mutableListOf()
        channels.addAll(buf.readChannels())
        try {
            val bytes = ByteArray(buf.readableBytes())
            buf.getBytes(buf.readerIndex(), bytes)
            ByteArrayInputStream(bytes.decompress()).use {
                ObjectInputStream(it).use { ois ->
                    val size = ois.readInt()
                    for (i in 0 until size) {
                        val name = ois.readUTF()
                        val uuid = UUID.fromString(ois.readUTF())
                        val port = ois.readInt()
                        val channel =
                            if (port == -1) null else channels.firstOrNull { channel -> channel.getPort() == port }
                        players.add(NettyPlayerImpl(name, uuid, channel))
                    }
                }
            }
        } catch (e: Exception) {
            println("[ChannelListPacket::WARN]")
        }
    }
}