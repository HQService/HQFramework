package kr.hqservice.framework.velocity.core.netty

import io.netty.util.concurrent.ScheduledFuture
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class PingPongManagementThread(
    private val wrapper: ChannelWrapper,

    private val intervalMillis: Long = 1000L,
    private val timeoutMillis: Long = 3000L,
    private val maxMissed: Int = 3,
) {
    private var task: ScheduledFuture<*>? = null
    private val running = AtomicBoolean(false)

    @Volatile private var awaiting = false
    @Volatile private var lastSendNano: Long = 0L
    @Volatile private var missed = 0

    fun start() {
        if (!running.compareAndSet(false, true)) return
        val el = wrapper.channel.eventLoop()
        task = el.scheduleAtFixedRate({
            if (!wrapper.channel.isActive || wrapper.handler.connectionState != ConnectionState.CONNECTED) {
                stop()
                return@scheduleAtFixedRate
            }

            if (awaiting) {
                val waited = (System.nanoTime() - lastSendNano) / 1_000_000 // ms
                if (waited >= timeoutMillis) {
                    missed += 1
                    awaiting = false
                    if (missed >= maxMissed) {
                        // 연속 미수신 임계치 초과: 연결 종료 또는 알림
                        wrapper.channel.close() // 혹은 wrapper.onHeartbeatFail()
                        stop()
                        return@scheduleAtFixedRate
                    }
                } else return@scheduleAtFixedRate

            }

            // 새 ping 전송
            val sendTime = System.nanoTime()
            val pkt = PingPongPacket(-1L, System.currentTimeMillis())
            awaiting = true
            lastSendNano = sendTime

            // 콜백: pong 수신 시 RTT 계산 + 상태 초기화
            wrapper.startCallback(pkt, PingPongPacket::class) { pong ->
                val rttMs = (System.nanoTime() - sendTime) / 1_000_000
                wrapper.pingCalculator.process(rttMs)
                missed = 0
                awaiting = false

                // echo 응답 (기존 로직 유지)
                val rt = PingPongPacket(pong.time, -1L).apply { setCallbackResult(true) }
                // 반드시 eventLoop에서 write (우리는 이미 eventLoop 안)
                wrapper.sendPacket(rt)
            }

        }, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        task?.cancel(false)
        task = null
        awaiting = false
        missed = 0
    }
}