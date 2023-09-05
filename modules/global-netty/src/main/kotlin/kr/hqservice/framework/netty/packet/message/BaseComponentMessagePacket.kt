package kr.hqservice.framework.netty.packet.message

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readPlayers
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writePlayers
import kr.hqservice.framework.netty.packet.extension.writeString
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.chat.*
import java.awt.Color
import java.util.regex.Pattern

class BaseComponentMessagePacket(
    var message: BaseComponent,
    var logging: Boolean,
    var receivers: List<NettyPlayer>
) : Packet() {

    override fun write(buf: ByteBuf) {
        val compress = ComponentSerializer.toString(message)
        val hex = compress.toHex()
        println("[write] before: $compress")
        println("[write] after: $hex")
        buf.writeString(hex)
        buf.writeBoolean(logging)
        buf.writePlayers(receivers)
    }

    override fun read(buf: ByteBuf) {
        buf.readString().apply {
            val parse = ComponentSerializer.parse(this).first()
            println("[read] before: $this")
            println("[read] after: $parse")
            message = parse
        }
        logging = buf.readBoolean()
        receivers = buf.readPlayers()
    }
}

private val pattern = Pattern.compile("§x§[a-zA-Z0-9]+§[a-zA-Z0-9]+§[a-zA-Z0-9]+§[a-zA-Z0-9]+§[a-zA-Z0-9]+§[a-zA-Z0-9]+")

private fun String.toHex(): String {
    val matcher = pattern.matcher(this)

    return matcher.replaceAll { result ->
        val group = result.group()
        /*val hexText = group.replace("§", "").replace("x", "#")
        println("Hex: $hexText")
        val javaColor = Color(hexText.toInt(16))
        val chatColor = ChatColor.of(javaColor)*/
        group.replace("§", "").replace("x", "#")
    }
}