package kr.hqservice.framework.netty.pipeline

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.ReadTimeoutException
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.channel.DisconnectHandler
import kr.hqservice.framework.netty.channel.PacketPreprocessHandler
import kr.hqservice.framework.netty.packet.AbstractPacket
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import java.io.IOException
import java.util.logging.Logger

class BossHandler(
    channel: Channel,
    private val logger: Logger
) : ChannelInboundHandlerAdapter() {
    private var preprocessHandler: PacketPreprocessHandler? = null
    private var disconnectHandler: DisconnectHandler? = null

    val channel: ChannelWrapper

    var connectionState: ConnectionState
        private set


    init {
        this.channel = ChannelWrapper(logger,this, channel)
        connectionState = ConnectionState.IDLE
    }

    fun setDisconnectionHandler(handler: (ChannelWrapper)->Unit) {
        this.disconnectHandler = object: DisconnectHandler {
            override fun onDisconnect(channel: ChannelWrapper) {
                handler(channel)
            }
        }
    }

    fun setPacketPreprocessHandler(handler: (AbstractPacket, ChannelWrapper)-> Unit) {
        this.preprocessHandler = object: PacketPreprocessHandler {
            override fun preprocess(packet: AbstractPacket, channel: ChannelWrapper) {
                handler(packet, channel)
            }
        }
    }

    fun setConnectionState(state: ConnectionState) {
        connectionState = state
        logger.info("Internal netty connection state is now: ${state.name}")
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if(connectionState != ConnectionState.CONNECTED && msg !is HandShakePacket) {
            logger.severe("received packet before handshake. received packet dropped")
            return
        }

        val packet = msg as AbstractPacket
        preprocessHandler?.preprocess(packet, channel)

        if(packet.isCallbackResult() && channel.callbackContainer.complete(packet))
            return

        Direction.INBOUND.onPacketReceived(packet, channel)
    }

    @Deprecated("")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        when (cause) {
            is IOException -> logger.severe("i/o exception")
            is IllegalArgumentException -> logger.severe("illegal argument exception: ${cause.message}")
            is ReadTimeoutException -> logger.severe("read timeout exception")
            else -> logger.severe("other exception")
        }
        cause.printStackTrace()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        setConnectionState(ConnectionState.IDLE)
        disconnectHandler?.onDisconnect(channel)
    }
}