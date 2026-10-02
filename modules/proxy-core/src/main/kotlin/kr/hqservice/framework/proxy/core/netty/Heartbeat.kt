package kr.hqservice.framework.proxy.core.netty

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Heartbeat(private val wrapper: ChannelWrapper) {
    @Volatile
    private var task: ScheduledFuture<*>? = null

    fun start() {
        val channel = wrapper.channel
        task = channel.eventLoop().scheduleAtFixedRate(::beat, 1, 1, TimeUnit.SECONDS)
        channel.closeFuture().addListener { stop() }
    }

    fun stop() {
        task?.cancel(false)
    }

    private fun beat() {
        if (wrapper.handler.connectionState != ConnectionState.CONNECTED) {
            stop()
            return
        }

        wrapper.startCallback(
            PingPongPacket(-1L, System.currentTimeMillis()),
            PingPongPacket::class
        ) { packet ->
            val ping = System.currentTimeMillis() - packet.receivedTime
            wrapper.pingCalculator.process(ping)
            val rt = PingPongPacket(packet.time, -1L)
            rt.setCallbackResult(true)
            wrapper.sendPacket(rt)
        }
    }
}
