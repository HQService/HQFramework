package kr.hqservice.framework.netty.channel

import io.netty.channel.Channel
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kotlin.reflect.KClass

class ChannelWrapper(
    val handler: BossHandler,
    val channel: Channel,
    val port: Int = -1
) {

    private val callbackContainer = CallbackContainer()
    private var enabled = true

    fun<T : AbstractPacket> startCallback(packet: AbstractPacket, type: KClass<T>, onReceived: PacketCallback<T>) {
        callbackContainer.addOnQueue(this, packet, type, onReceived)
    }

    fun sendPacket(packet: AbstractPacket) {
        if(enabled) {

        }
    }

}