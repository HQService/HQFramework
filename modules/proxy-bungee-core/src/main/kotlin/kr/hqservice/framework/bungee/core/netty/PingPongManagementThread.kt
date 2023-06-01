package kr.hqservice.framework.bungee.core.netty

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import java.util.concurrent.TimeUnit

class PingPongManagementThread(
    private val wrapper: ChannelWrapper
) : Thread() {
    override fun run() {
        while(wrapper.channel.isOpen && wrapper.channel.isActive) {
            try {
                TimeUnit.SECONDS.sleep(1)
            } catch (e: InterruptedException) { e.printStackTrace() }

            if(wrapper.handler.connectionState != ConnectionState.CONNECTED)
                return

            wrapper.startCallback(
                PingPongPacket(-1L, System.currentTimeMillis()),
                PingPongPacket::class) { packet ->
                val ping = System.currentTimeMillis() - packet.receivedTime
                wrapper.pingCalculator.process(ping)
                val rt = PingPongPacket(packet.time, -1L)
                rt.setCallbackResult(true)
                wrapper.sendPacket(rt)
            }
        }
    }
}