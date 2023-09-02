package kr.hqservice.framework.netty.packet.message

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readPlayers
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.extension.writePlayers
import kr.hqservice.framework.netty.packet.extension.writeString
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.*
import net.md_5.bungee.chat.*

private val parser = JsonParser()
private val gson = GsonBuilder()
    .registerTypeAdapter(
        BaseComponent::class.java, ComponentSerializer()
    ).registerTypeAdapter(
        TextComponent::class.java, TextComponentSerializer()
    ).registerTypeAdapter(
        TranslatableComponent::class.java, TranslatableComponentSerializer()
    ).registerTypeAdapter(
        KeybindComponent::class.java, KeybindComponentSerializer()
    ).registerTypeAdapter(
        ScoreComponent::class.java, ScoreComponentSerializer()
    ).registerTypeAdapter(
        SelectorComponent::class.java, SelectorComponentSerializer()
    ).registerTypeAdapter(
        Entity::class.java, EntitySerializer()
    ).registerTypeAdapter(
        Text::class.java, TextSerializer()
    ).registerTypeAdapter(
        Item::class.java, ItemSerializer()
    ).registerTypeAdapter(
        ItemTag::class.java, ItemTag.Serializer()
    ).setLenient().create()

private fun BaseComponent.toCompress(): String {
    return gson.toJson(this)
}

private fun String.toDecompress(): BaseComponent {
    val jsonElement = parser.parse(this)
    return if (jsonElement.isJsonArray) {
        gson.fromJson(jsonElement, Array<BaseComponent>::class.java)
    } else {
        arrayOf(gson.fromJson(jsonElement, BaseComponent::class.java))
    }.first()
}

class BaseComponentMessagePacket(
    var message: BaseComponent,
    var logging: Boolean,
    var receivers: List<NettyPlayer>
) : Packet() {

    override fun write(buf: ByteBuf) {
        // buf.writeString(ComponentSerializer.toString(message))
        val compress = message.toCompress()
        buf.writeString(compress)
        println(compress)
        buf.writeBoolean(logging)
        buf.writePlayers(receivers)
    }

    override fun read(buf: ByteBuf) {
        message = buf.readString().toDecompress()
        logging = buf.readBoolean()
        receivers = buf.readPlayers()
    }
}