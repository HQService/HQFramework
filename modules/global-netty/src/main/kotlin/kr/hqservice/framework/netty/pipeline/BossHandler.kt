package kr.hqservice.framework.netty.pipeline

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.ReadTimeoutException
import io.netty.util.concurrent.DefaultEventExecutorGroup
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.channel.DisconnectHandler
import kr.hqservice.framework.netty.channel.PacketPreprocessHandler
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class BossHandler(
    channel: Channel,
    private val logger: Logger,
    private val expectedSecret: String? = null
) : ChannelInboundHandlerAdapter() {
    private lateinit var channelScope: ChannelScope
    private lateinit var serialized: CoroutineDispatcher

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
        channelScope.scope.launch(serialized) {
            channelScope.gate.withPermit {
                preprocessHandler?.preprocess(packet, channel)
                if (packet.isCallbackResult() && channel.callbackContainer.complete(packet)) return@launch

                Direction.INBOUND.onPacketReceived(packet, channel)
            }
        }
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
        disconnectHandler?.onDisconnect(channel)
        channelScope.close()
        super.channelInactive(ctx)
    }
}

val blockingGroup = DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors())
val blockingDispatcher = blockingGroup.asCoroutineDispatcher()

class ChannelScope(ctx: ChannelHandlerContext, logger: Logger) {
    val job = SupervisorJob()
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.log(Level.SEVERE, "error: ${throwable.message}", throwable)
    }
    val scope = CoroutineScope(job + CoroutineName("ch-${ctx.channel().id()}") + exceptionHandler)
    val gate = Semaphore(64)
    fun close() = job.cancel()
}