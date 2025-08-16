package kr.hqservice.framework.database.repository.player.lock

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

class IOGate(hikariPoolSize: Int, margin: Int) {
    enum class GateType {
        SAVE, LOAD, PRELOAD
    }

    val saveGate: Semaphore
    val loadGate: Semaphore
    val preloadGate: Semaphore

    init {
        val usable = (hikariPoolSize - margin).coerceAtLeast(3)
        var save = (usable * 0.5).roundToInt().coerceAtLeast(1)
        var load = (usable * 0.3).roundToInt().coerceAtLeast(1)
        var preload = usable - save - load

        if (preload < 1) {
            var deficit = 1 - preload
            while (deficit > 0) {
                if (save >= load && save > 1) {
                    save--
                    deficit--
                } else if (load > 1) {
                    load--
                    deficit--
                } else break
            }
            preload = 1
        }

        saveGate = Semaphore(save)
        loadGate = Semaphore(load)
        preloadGate = Semaphore(preload)
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
                GateType.PRELOAD -> preloadGate.let {
                    it.withPermit { action(it) }
                }
            }
        }

        return if (timeout <= 0) result.invoke()
        else withTimeout(timeout) { result.invoke() }
    }
}