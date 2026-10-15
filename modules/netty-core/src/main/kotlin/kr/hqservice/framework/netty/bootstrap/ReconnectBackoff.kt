package kr.hqservice.framework.netty.bootstrap

import kotlin.random.Random

class ReconnectBackoff(
    private val baseMillis: Long = 1_000,
    private val maxMillis: Long = 30_000,
    private val jitter: () -> Double = { Random.nextDouble() },
) {
    private var attempt = 0

    @Synchronized
    fun nextDelayMillis(): Long {
        val exponential = baseMillis shl minOf(attempt, 20)
        attempt++
        val capped = minOf(exponential, maxMillis)
        return capped + (capped * 0.2 * jitter()).toLong()
    }

    @Synchronized
    fun reset() {
        attempt = 0
    }
}
