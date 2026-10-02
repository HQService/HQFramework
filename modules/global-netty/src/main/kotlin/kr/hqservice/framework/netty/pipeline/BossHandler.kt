package kr.hqservice.framework.netty.pipeline

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.ReadTimeoutException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.channel.DisconnectHandler
import kr.hqservice.framework.netty.channel.PacketPreprocessHandler
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger

class BossHandler(
    channel: Channel,
    private val logger: Logger,
    private val expectedSecret: String?,
    private val blockingDispatcher: CoroutineDispatcher
) : ChannelInboundHandlerAdapter() {
    private companion object {
        const val PAUSE_ABOVE_PENDING = 256
        const val RESUME_BELOW_PENDING = 64
    }

    private lateinit var channelScope: ChannelScope
    private lateinit var serialized: CoroutineDispatcher
    private val pending = AtomicInteger()

    @Volatile
    private var preprocessHandler: PacketPreprocessHandler? = null
    @Volatile
    private var disconnectHandler: DisconnectHandler? = null

    val channel: ChannelWrapper = ChannelWrapper(logger, this, channel)
    @Volatile
    var connectionState: ConnectionState = ConnectionState.IDLE
        set(value) {
            field = value
            logger.info("Internal netty connection state is now: ${value.name}")
        }

    fun setDisconnectionHandler(handler: (ChannelWrapper) -> Unit) {
        this.disconnectHandler = object : DisconnectHandler {
            override fun onDisconnect(channel: ChannelWrapper) {
                handler(channel)
            }
        }
    }

    fun setPacketPreprocessHandler(handler: (Packet, ChannelWrapper) -> Unit) {
        this.preprocessHandler = object : PacketPreprocessHandler {
            override fun preprocess(packet: Packet, channel: ChannelWrapper) {
                handler(packet, channel)
            }
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is HandShakePacket && connectionState != ConnectionState.CONNECTED) {
            if (!secretAccepted(msg.secret)) {
                logger.warning("rejected handshake from ${ctx.channel().remoteAddress()}: bad secret")
                ctx.close()
                return
            }
            connectionState = ConnectionState.CONNECTED
        }

        if (connectionState != ConnectionState.CONNECTED && msg !is HandShakePacket) {
            logger.severe("received packet before handshake. received packet dropped")
            return
        }

        val packet = msg as Packet
        if (pending.incrementAndGet() > PAUSE_ABOVE_PENDING) ctx.channel().config().isAutoRead = false
        channelScope.scope.launch(serialized) {
            try {
                preprocessHandler?.preprocess(packet, channel)
                if (packet.isCallbackResult() && channel.callbackContainer.complete(packet)) return@launch

                Direction.INBOUND.onPacketReceived(packet, channel)
            } finally {
                if (pending.decrementAndGet() < RESUME_BELOW_PENDING) resumeReading(ctx)
            }
        }
    }

    private fun resumeReading(ctx: ChannelHandlerContext) {
        if (ctx.channel().config().isAutoRead) return
        val executor = ctx.executor()
        if (executor.inEventLoop()) enableAutoRead(ctx) else executor.execute { enableAutoRead(ctx) }
    }

    private fun enableAutoRead(ctx: ChannelHandlerContext) {
        if (pending.get() < RESUME_BELOW_PENDING) ctx.channel().config().isAutoRead = true
    }

    private fun secretAccepted(secret: String) =
        expectedSecret == null || MessageDigest.isEqual(expectedSecret.toByteArray(), secret.toByteArray())

    @Deprecated("")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        when (cause) {
            is IOException -> logger.severe("i/o exception")
            is IllegalArgumentException -> logger.severe("illegal argument exception: ${cause.message}")
            is ReadTimeoutException -> {
                logger.severe("read timeout exception")
                ctx.close()
            }
            else -> logger.severe("other exception")
        }
        cause.printStackTrace()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        channelScope = ChannelScope(ctx, logger)
        serialized = blockingDispatcher.limitedParallelism(1)
        ctx.executor().schedule({
            if (connectionState != ConnectionState.CONNECTED && ctx.channel().isOpen) {
                logger.warning("closing ${ctx.channel().remoteAddress()}: no handshake within 10s")
                ctx.close()
            }
        }, 10, TimeUnit.SECONDS)
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        connectionState = ConnectionState.IDLE
        try {
            disconnectHandler?.onDisconnect(channel)
        } finally {
            channelScope.close()
            super.channelInactive(ctx)
        }
    }
}

class ChannelScope(ctx: ChannelHandlerContext, logger: Logger) {
    val job = SupervisorJob()
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.log(Level.SEVERE, "error: ${throwable.message}", throwable)
    }
    val scope = CoroutineScope(job + CoroutineName("ch-${ctx.channel().id()}") + exceptionHandler)
    fun close() = job.cancel()
}