package kr.hqservice.framework.database.redis

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.time.Duration

class RedisSettings(val uri: String, val keyPrefix: String, val dataTtl: Duration) {
    val enabled: Boolean get() = uri.isNotBlank()

    fun key(vararg parts: String): String = (listOf(keyPrefix) + parts).joinToString(":")

    companion object {
        fun from(config: HQYamlConfiguration): RedisSettings {
            val ttlSeconds = config.getLong("redis.data-ttl-seconds", 3600L)
            check(ttlSeconds > 0) { "redis.data-ttl-seconds must be positive" }
            return RedisSettings(
                config.getString("redis.uri", ""),
                config.getString("redis.key-prefix", "hq"),
                Duration.ofSeconds(ttlSeconds),
            )
        }
    }
}
