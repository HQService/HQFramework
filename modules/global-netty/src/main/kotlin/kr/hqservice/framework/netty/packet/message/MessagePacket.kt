package kr.hqservice.framework.netty.packet.message

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readPlayers
import kr.hqservice.framework.netty.packet.extension.writePlayers
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

class MessagePacket(
    var message: BaseComponent,
    var logging: Boolean,
    var receivers: List<NettyPlayer>,
) : Packet() {
    override fun write(buf: ByteBuf) {
        val byteArray = BaseComponent.toPlainText(message).toByteArray().compress()
        buf.writeInt(byteArray.size)
        buf.writeBytes(byteArray)
        buf.writeBoolean(logging)
        buf.writePlayers(receivers)

        //val base64 = Base64Coder.encodeLines(byteArray).chunkedSequence(100)
        //val base64 = Base64Coder.encodeLines(byteArray).split("\n").filter { it.isNotBlank() && it.isNotEmpty() }
        //buf.writeStringArray(base64.toList().toTypedArray())
    }

    override fun read(buf: ByteBuf) {
        val bytes = ByteArray(buf.readInt())
        buf.readBytes(bytes)
        message = ComponentSerializer.parse(
            bytes.decompress().toString(Charsets.UTF_8)
        ).first()
        logging = buf.readBoolean()
        receivers = buf.readPlayers()

        /*val base64 = buf.readStringArray().joinToString("")
        val bytes = Base64Coder.decodeLines(base64).decompress()
        message = ComponentSerializer.parse(
            bytes.toString(Charsets.UTF_8)
        ).first()*/
    }
}