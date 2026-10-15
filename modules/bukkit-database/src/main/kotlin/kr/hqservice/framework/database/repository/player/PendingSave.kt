package kr.hqservice.framework.database.repository.player

internal data class PendingSave<V : Any>(
    val value: V,
    val generation: Long,
    val fingerprint: Any?,
    val fingerprintChanged: Boolean,
)
