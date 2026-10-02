package kr.hqservice.framework.database.repository.player

import java.time.Duration

sealed class SavePolicy {
    data class Periodic(val dirtyInterval: Duration?, val fullInterval: Duration?, val batchSize: Int?) : SavePolicy() {
        init {
            require(dirtyInterval == null || dirtyInterval > Duration.ZERO) { "dirtyInterval must be positive" }
            require(fullInterval == null || fullInterval > Duration.ZERO) { "fullInterval must be positive" }
            require(batchSize == null || batchSize > 0) { "batchSize must be positive" }
        }
    }

    object OnQuitOnly : SavePolicy()

    companion object {
        fun periodic(dirtyInterval: Duration? = null, fullInterval: Duration? = null, batchSize: Int? = null): SavePolicy =
            Periodic(dirtyInterval, fullInterval, batchSize)

        fun onQuitOnly(): SavePolicy = OnQuitOnly
    }
}
