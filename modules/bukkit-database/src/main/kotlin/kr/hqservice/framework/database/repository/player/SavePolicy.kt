package kr.hqservice.framework.database.repository.player

import java.time.Duration

sealed class SavePolicy {
    data class Periodic(val dirtyInterval: Duration?, val fullInterval: Duration?, val batchSize: Int?) : SavePolicy()

    object OnQuitOnly : SavePolicy()

    companion object {
        fun periodic(dirtyInterval: Duration? = null, fullInterval: Duration? = null, batchSize: Int? = null): SavePolicy =
            Periodic(dirtyInterval, fullInterval, batchSize)

        fun onQuitOnly(): SavePolicy = OnQuitOnly
    }
}
