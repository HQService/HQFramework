package kr.hqservice.framework.netty.packet.extension

import io.netty.buffer.ByteBuf
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