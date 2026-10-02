package kr.hqservice.framework.netty.packet.extension

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import java.util.*
import kotlin.experimental.and

private const val MAX_STRING_CHARS = 32767
private const val MAX_STRING_BYTES = MAX_STRING_CHARS * 4
private const val MAX_COLLECTION_SIZE = 65536

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
    if (string.length > MAX_STRING_CHARS)
        throw IllegalArgumentException("cannot send string longer than $MAX_STRING_CHARS (got ${string.length} characters)")
    val bytes = string.toByteArray(Charsets.UTF_8)
    writeVarInt(bytes.size)
    writeBytes(bytes)
}

fun ByteBuf.readString(): String {
    val length = readVarInt(5)
    if (length !in 0 .. MAX_STRING_BYTES) {
        throw IllegalArgumentException("invalid string byte length: $length")
    }
    if (length > readableBytes()) {
        throw IllegalArgumentException("string length $length exceeds remaining buffer (${readableBytes()})")
    }
    val bytes = ByteArray(length)
    readBytes(bytes)
    return bytes.toString(Charsets.UTF_8)
}

fun ByteBuf.writeStringArray(array: Array<String>) {
    writeVarInt(array.size)
    array.forEach(::writeString)
}

fun ByteBuf.readStringArray(): Array<String> {
    val length = readVarInt(5)
    if (length !in 0 .. MAX_COLLECTION_SIZE) {
        throw IllegalArgumentException("invalid string array size: $length")
    }
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
    writeBoolean(nettyChannel != null)
    if (nettyChannel != null) {
        writeString(nettyChannel.getName())
        writeInt(nettyChannel.getPort())
    }
}

fun ByteBuf.readChannel(): NettyChannel? {
    if (!readBoolean()) return null
    val name = readString()
    val port = readInt()
    return NettyChannelImpl(port, name)
}

fun ByteBuf.writeChannels(nettyChannels: List<NettyChannel>) {
    writeInt(nettyChannels.size)
    nettyChannels.forEach(::writeChannel)
}

fun ByteBuf.readChannels(): List<NettyChannel> {
    val size = readInt()
    if (size !in 0 .. MAX_COLLECTION_SIZE) {
        throw IllegalArgumentException("invalid channel list size: $size")
    }
    return List(size) { readChannel()!! }
}

fun ByteBuf.writePlayer(nettyPlayer: NettyPlayer) {
    writeString(nettyPlayer.getName())
    writeString(nettyPlayer.getDisplayName())
    writeUUID(nettyPlayer.getUniqueId())
    nettyPlayer.getChannel().apply(::writeChannel)
}

fun ByteBuf.readPlayer(): NettyPlayer {
    return NettyPlayerImpl(
        readString(),
        readString(),
        readUUID(),
        readChannel()
    )
}

fun ByteBuf.writePlayers(nettyPlayers: List<NettyPlayer>) {
    writeInt(nettyPlayers.size)
    nettyPlayers.forEach(::writePlayer)
}

fun ByteBuf.readPlayers(): List<NettyPlayer> {
    try {
        val size = readInt()
        if (size !in 0 .. MAX_COLLECTION_SIZE) {
            throw IllegalArgumentException("invalid player list size: $size")
        }
        return List(size) { readPlayer() }
    } catch (e: Exception) {
        throw DecoderException("players payload corrupt", e)
    }
}
