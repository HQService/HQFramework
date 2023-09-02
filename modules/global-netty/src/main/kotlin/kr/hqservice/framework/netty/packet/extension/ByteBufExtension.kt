package kr.hqservice.framework.netty.packet.extension

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.experimental.and

fun ByteBuf.writeVarInt(value: Int) {
    var current = value
    do {
        var part = current.and(0x7F)
        current = current.ushr(7)
        if (current != 0)
            part = part.or(0x80)
        writeByte(part)
    } while (current != 0)
}

fun ByteBuf.readVarInt(maxBytes: Int): Int {
    var `in`: Byte
    var out = 0
    var bytes = 0
    do {
        `in` = readByte()
        out = out.or(`in`.and(Byte.MAX_VALUE).toInt().shl(bytes++ * 7))
        if (bytes > maxBytes)
            throw RuntimeException("VarInt too big")
    } while (`in`.and(0x80.toByte()).toInt() == 128)
    return out
}

fun ByteBuf.writeString(string: String) {
    if (string.length > 32767)
        throw IllegalArgumentException("cannot send string longer than Short.MAX_VALUE (got ${string.length} characters)")
    val bytes = string.toByteArray(Charsets.UTF_8).compress()
    writeVarInt(bytes.size)
    writeBytes(bytes)
}

fun ByteBuf.readString(): String {
    val length = readVarInt(5)
    val bytes = ByteArray(length)
    readBytes(bytes)
    return bytes.decompress().toString(Charsets.UTF_8)
}

fun ByteBuf.writeStringArray(array: Array<String>) {
    writeVarInt(array.size)
    array.forEach(::writeString)
}

fun ByteBuf.readStringArray(): Array<String> {
    val length = readVarInt(5)
    return Array(length) { readString() }
}

fun ByteBuf.writeUUID(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}

fun ByteBuf.readUUID(): UUID {
    return UUID(readLong(), readLong())
}

fun ByteBuf.writeChannel(nettyChannel: NettyChannel?) {
    if (nettyChannel == null) writeString("null-channel")
    else {
        writeString(nettyChannel.getName())
        writeInt(nettyChannel.getPort())
    }
}

fun ByteBuf.readChannel(): NettyChannel? {
    val name = readString()
    if (name == "null-channel") return null
    val port = readInt()
    return NettyChannelImpl(port, name)
}

fun ByteBuf.writeChannels(nettyChannels: List<NettyChannel>) {
    writeInt(nettyChannels.size)
    nettyChannels.forEach(::writeChannel)
}

fun ByteBuf.readChannels(): List<NettyChannel> {
    return List(readInt()) { readChannel()!! }
}

fun ByteBuf.writePlayer(nettyPlayer: NettyPlayer) {
    writeString(nettyPlayer.getName())
    writeUUID(nettyPlayer.getUniqueId())
    nettyPlayer.getChannel().apply(::writeChannel)
}

fun ByteBuf.readPlayer(): NettyPlayer {
    return NettyPlayerImpl(
        readString(),
        readUUID(),
        readChannel()
    )
}

fun ByteBuf.writePlayers(nettyPlayers: List<NettyPlayer>) {
    if (nettyPlayers.isEmpty()) writeBytes(ByteArray(0))
    else ByteArrayOutputStream().use {
        ObjectOutputStream(it).use { oos ->
            oos.writeInt(nettyPlayers.size)
            nettyPlayers.forEach { player ->
                oos.writeUTF(player.getName())
                oos.writeUTF(player.getUniqueId().toString())
                player.getChannel()?.apply {
                    oos.writeInt(getPort())
                    oos.writeUTF(getName())
                } ?: oos.writeInt(-1)
            }
        }
        val byteArray = it.toByteArray().compress()
        writeBytes(byteArray)
    }
}

fun ByteBuf.readPlayers(): List<NettyPlayer> {
    val players = mutableListOf<NettyPlayer>()
    try {
        val bytes = ByteArray(readableBytes())
        getBytes(readerIndex(), bytes)
        ByteArrayInputStream(bytes.decompress()).use {
            ObjectInputStream(it).use { ois ->
                val size = ois.readInt()
                for (i in 0 until size) {
                    val name = ois.readUTF()
                    val uuid = UUID.fromString(ois.readUTF())
                    val port = ois.readInt()
                    val channel =
                        if (port == -1) null else NettyChannelImpl(port, ois.readUTF())
                    players.add(NettyPlayerImpl(name, uuid, channel))
                }
            }
        }
    } catch (_: Exception) {
    }
    return players
}