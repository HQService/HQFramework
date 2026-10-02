package kr.hqservice.framework.netty.packet.message

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readChannel
import kr.hqservice.framework.netty.packet.extension.writeChannel
import kr.hqservice.framework.netty.pipeline.MAX_FRAME_BYTES
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

class BroadcastPacket(
    var message: Component,
    var logging: Boolean,
    var targetChannel: NettyChannel?
) : Packet() {
    override fun write(buf: ByteBuf) {
        val byteArray = GsonComponentSerializer.gson().serialize(message).toByteArray().compress()
        buf.writeInt(byteArray.size)
        buf.writeBytes(byteArray)
        buf.writeBoolean(logging)
        buf.writeChannel(targetChannel)
    }

    override fun read(buf: ByteBuf) {
        val size = buf.readInt()
        require(size in 0..MAX_FRAME_BYTES) { "message payload too large: $size" }
        val bytes = ByteArray(size)
        buf.readBytes(bytes)
        message = GsonComponentSerializer.gson().deserialize(
            bytes.decompress().toString(Charsets.UTF_8)
        )
        logging = buf.readBoolean()
        targetChannel = buf.readChannel()
    }
}