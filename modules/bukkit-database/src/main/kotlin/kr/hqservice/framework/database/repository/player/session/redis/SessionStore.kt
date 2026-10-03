package kr.hqservice.framework.database.repository.player.session.redis

import kr.hqservice.framework.database.repository.player.session.AcquireResult

interface SessionStore {
    suspend fun acquire(key: String, owner: String, leaseMillis: Long): AcquireResult

    suspend fun renew(keys: List<String>, owner: String, leaseMillis: Long)

    suspend fun commit(key: String, owner: String, expectedVersion: Long, leaseMillis: Long): Long?

    suspend fun release(key: String, owner: String): Boolean

    suspend fun verify(key: String, owner: String, expectedVersion: Long): Boolean

    suspend fun ownedVersion(key: String, owner: String): Long?
}
