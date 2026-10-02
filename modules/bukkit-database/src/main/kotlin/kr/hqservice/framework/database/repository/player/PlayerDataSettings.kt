package kr.hqservice.framework.database.repository.player

import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.time.Duration

class PlayerDataSettings(
    val backend: String,
    val lease: Duration,
    val renewInterval: Duration,
    val joinTimeout: Duration,
    val retryInterval: Duration,
    val dirtyFlushInterval: Duration,
    val fullFlushInterval: Duration,
) {
    companion object {
        fun from(config: HQYamlConfiguration): PlayerDataSettings {
            val backend = config.getString("player-data.backend", "database")
            if (backend != "database") {
                throw IllegalStateException("player-data.backend '$backend' is not supported in this version")
            }
            return PlayerDataSettings(
                backend,
                Duration.ofSeconds(config.getInt("player-data.lease-seconds", 30).toLong()),
                Duration.ofSeconds(config.getInt("player-data.renew-seconds", 10).toLong()),
                Duration.ofSeconds(config.getInt("player-data.join-timeout-seconds", 5).toLong()),
                Duration.ofMillis(config.getInt("player-data.retry-interval-millis", 200).toLong()),
                Duration.ofSeconds(config.getInt("player-data.dirty-flush-seconds", 5).toLong()),
                Duration.ofSeconds(config.getInt("player-data.full-flush-seconds", 60).toLong()),
            )
        }
    }
}
