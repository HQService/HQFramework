package kr.hqservice.framework.netty.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import kr.hqservice.framework.netty.math.PingCalculator
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

class ChannelWrapper(
    private val logger: Logger,
    val handler: BossHandler,
    val channel: Channel,
    var port: Int = -1
) {

    @Volatile
    private var enabled = true
    val callbackContainer = CallbackContainer(logger)
    val pingCalculator = PingCalculator()

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun <T : Packet> startCallback(packet: Packet, type: KClass<T>, onReceived: (packet: T) -> Unit) {
        val handler = object : PacketCallbackHandler<T> {
            override fun onCallbackReceived(packet: T) {
                onReceived(packet)
            }
        }
        callbackContainer.addOnQueue(this, packet, type, handler)
    }

    fun sendPacket(packet: Packet): Boolean = writePacket(packet) != null

    internal fun writePacket(packet: Packet): ChannelFuture? {
        if (!enabled) return null
        if (handler.connectionState != ConnectionState.CONNECTED) {
            logger.severe("Some logic tried to send packet before connection established or disconnected. (Packet: ${packet::class.simpleName})")
            return null
        }
        return channel.writeAndFlush(packet).addListener { future ->
            if (!future.isSuccess && channel.isActive) {
                logger.log(Level.WARNING, "failed to send ${packet::class.simpleName}", future.cause())
            }
        }
    }
}