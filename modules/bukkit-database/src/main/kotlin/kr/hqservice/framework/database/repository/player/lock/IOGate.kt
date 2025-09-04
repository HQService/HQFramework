package kr.hqservice.framework.database.repository.player.lock

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

class IOGate(hikariPoolSize: Int, margin: Int) {
    enum class GateType {
        SAVE, LOAD
    }

    val saveGate: Semaphore
    val loadGate: Semaphore

    init {
        val usable = (hikariPoolSize - margin).coerceAtLeast(3)
        var save = (usable * 0.6).roundToInt().coerceAtLeast(1)
        var load = (usable * 0.4).roundToInt().coerceAtLeast(1)

        saveGate = Semaphore(save)
        loadGate = Semaphore(load)
    }

    suspend inline fun <T> withPermit(type: GateType, timeout: Long = 800, crossinline action: suspend Semaphore.() -> T): T {
        val result: suspend () -> T = {
            when (type) {
                GateType.SAVE -> saveGate.let {
                    it.withPermit { action(it) }
                }
                GateType.LOAD -> loadGate.let {
                    it.withPermit { action(it) }
                }
            }
        }

        return if (timeout <= 0) result.invoke()
        else withTimeout(timeout) { result.invoke() }
    }
}