package kr.hqservice.framework.database.repository.player.cache

import kotlinx.coroutines.future.await
import kr.hqservice.framework.database.redis.RedisProvider
import java.time.Duration

interface PlayerDataCache {
    suspend fun read(key: String): ByteArray?

    suspend fun write(key: String, value: ByteArray)

    suspend fun expire(key: String, ttl: Duration)

    suspend fun delete(key: String)
}

class LettucePlayerDataCache(private val provider: RedisProvider) : PlayerDataCache {
    override suspend fun read(key: String): ByteArray? = provider.connection().async().get(key).await()

    override suspend fun write(key: String, value: ByteArray) {
        provider.connection().async().set(key, value).await()
    }

    override suspend fun expire(key: String, ttl: Duration) {
        provider.connection().async().pexpire(key, ttl.toMillis()).await()
    }

    override suspend fun delete(key: String) {
        provider.connection().async().del(key).await()
    }
}
