package kr.hqservice.framework.netty.packet.message

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readChannel
import kr.hqservice.framework.netty.packet.extension.writeChannel
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

class BroadcastPacket(
    var message: BaseComponent,
    var logging: Boolean,
    var targetChannel: NettyChannel?
) : Packet() {
    override fun write(buf: ByteBuf) {
        val byteArray = BaseComponent.toPlainText(message).toByteArray().compress()
        buf.writeInt(byteArray.size)
        buf.writeBytes(byteArray)
        buf.writeBoolean(logging)
        buf.writeChannel(targetChannel)
    }

    override fun read(buf: ByteBuf) {
        val bytes = ByteArray(buf.readInt())
        buf.readBytes(bytes)
        message = ComponentSerializer.parse(
            bytes.decompress().toString(Charsets.UTF_8)
        ).first()

        logging = buf.readBoolean()
        targetChannel = buf.readChannel()
    }
}