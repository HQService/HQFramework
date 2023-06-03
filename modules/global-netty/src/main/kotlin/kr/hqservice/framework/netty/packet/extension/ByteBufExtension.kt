package kr.hqservice.framework.netty.packet.extension

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import java.util.UUID
import kotlin.experimental.and

fun ByteBuf.writeVarInt(value: Int) {
    var current = value
    do {
        var part = current.and(0x7F)
        current = current.ushr(7)
        if(current != 0)
            part = part.or(0x80)
        writeByte(part)
    } while(current != 0)
}

fun ByteBuf.readVarInt(maxBytes: Int): Int {
    var `in`: Byte
    var out = 0
    var bytes = 0
    do {
        `in` = readByte()
        out = out.or(`in`.and(Byte.MAX_VALUE).toInt().shl(bytes++ * 7))
        if(bytes > maxBytes)
            throw RuntimeException("VarInt too big")
    } while(`in`.and(0x80.toByte()).toInt() == 128)
    return out
}

fun ByteBuf.writeString(string: String) {
    if (string.length > 32767)
        throw IllegalArgumentException("cannot send string longer than Short.MAX_VALUE (got ${string.length} characters)")
    val bytes = string.toByteArray(Charsets.UTF_8)
    writeVarInt(bytes.size)
    writeBytes(bytes)
}

fun ByteBuf.readString(): String {
    val length = readVarInt(5)
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
    if(nettyChannel == null) writeString("null-channel")
    else {
        writeString(nettyChannel.getName())
        writeInt(nettyChannel.getPort())
    }
}

fun ByteBuf.readChannel(): NettyChannel? {
    val name = readString()
    if(name == "null-channel") return null
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