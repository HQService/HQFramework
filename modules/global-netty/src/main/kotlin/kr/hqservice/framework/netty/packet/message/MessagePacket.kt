package kr.hqservice.framework.netty.packet.message

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.*
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder

class MessagePacket(
    var message: BaseComponent,
    var logging: Boolean,
    var receivers: List<NettyPlayer>,
) : Packet() {
    override fun write(buf: ByteBuf) {
        val byteArray = ComponentSerializer.toString(message).toByteArray().compress()
        val base64 = Base64Coder.encodeLines(byteArray).split("\n").filter { it.isNotBlank() && it.isNotEmpty() }
        buf.writeStringArray(base64.toTypedArray())

        buf.writeBoolean(logging)
        buf.writePlayers(receivers)
    }

    override fun read(buf: ByteBuf) {
        val base64 = buf.readStringArray().joinToString("\n")
        val bytes = Base64Coder.decodeLines(base64).decompress()
        message = ComponentSerializer.parse(
            bytes.toString(Charsets.UTF_8)
        ).first()
        logging = buf.readBoolean()
        receivers = buf.readPlayers()
    }
}