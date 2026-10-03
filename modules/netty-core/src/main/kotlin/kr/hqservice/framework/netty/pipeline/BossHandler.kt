package kr.hqservice.framework.netty.pipeline

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.ReadTimeoutException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.channel.DisconnectHandler
import kr.hqservice.framework.netty.channel.PacketPreprocessHandler
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketEventPolicy
import kr.hqservice.framework.netty.packet.server.PingPongPacket
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
        if (packet is PingPongPacket) {
            channelScope.scope.launch(start = CoroutineStart.UNDISPATCHED) { deliver(packet) }
            return
        }
        val pendingNow = pending.incrementAndGet()
        if (pendingNow > BACKPRESSURE_PAUSE_PENDING && ctx.channel().config().isAutoRead) {
            ctx.channel().config().isAutoRead = false
            logger.warning("channel ${ctx.channel().id()} paused reading: $pendingNow packets pending")
        }
        channelScope.scope.launch(serialized) {
            try {
                if (!PacketEventPolicy.isInternal(packet)) preprocessHandler?.preprocess(packet, channel)
                deliver(packet)
            } finally {
                if (pending.decrementAndGet() < BACKPRESSURE_RESUME_PENDING) resumeReading(ctx)
            }
        }
    }

    private suspend fun deliver(packet: Packet) {
        if (packet.isCallbackResult() && channel.callbackContainer.complete(packet)) return
        Direction.INBOUND.onPacketReceived(packet, channel)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext) {
        if (ctx.channel().isWritable) logger.info("channel ${ctx.channel().id()} is writable again")
        else logger.warning("channel ${ctx.channel().id()} outbound buffer is full; the peer is reading slowly")
        super.channelWritabilityChanged(ctx)
    }

    private fun resumeReading(ctx: ChannelHandlerContext) {
        if (ctx.channel().config().isAutoRead) return
        val executor = ctx.executor()
        if (executor.inEventLoop()) enableAutoRead(ctx) else executor.execute { enableAutoRead(ctx) }
    }

    private fun enableAutoRead(ctx: ChannelHandlerContext) {
        if (pending.get() < BACKPRESSURE_RESUME_PENDING) ctx.channel().config().isAutoRead = true
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
            channel.callbackContainer.failAll()
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