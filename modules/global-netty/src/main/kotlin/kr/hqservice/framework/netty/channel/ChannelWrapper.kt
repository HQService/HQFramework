package kr.hqservice.framework.netty.channel

import io.netty.channel.Channel
import kr.hqservice.framework.netty.math.PingCalculator
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import java.util.logging.Logger
import kotlin.reflect.KClass

class ChannelWrapper(
    private val logger: Logger,
    val handler: BossHandler,
    val channel: Channel,
    var port: Int = -1
) {

    private var enabled = true
    val callbackContainer = CallbackContainer()
    val pingCalculator = PingCalculator()

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun<T : Packet> startCallback(packet: Packet, type: KClass<T>, onReceived: (packet: T)-> Unit) {
        val handler = object: PacketCallbackHandler<T> {
            override fun onCallbackReceived(packet: T) {
                onReceived(packet)
            }
        }
        callbackContainer.addOnQueue(this, packet, type, handler)
    }

    fun sendPacket(packet: Packet) {
        if(enabled) {
            if(handler.connectionState == ConnectionState.CONNECTED) {
                channel.writeAndFlush(packet)
            } else logger.severe("Some logic tried to send packet before connection established or disconnected. (Packet: ${packet::class.simpleName})")
        }
    }
}