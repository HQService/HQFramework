package kr.hqservice.framework.database.redis

import kotlinx.serialization.json.Json
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.logging.Logger

@Configuration
class RedisConfig {
    @Bean
    fun provideRedisSettings(config: HQYamlConfiguration): RedisSettings = RedisSettings.from(config)

    @Bean
    fun provideRedisProvider(settings: RedisSettings, logger: Logger): RedisProvider = RedisProvider(settings, logger)

    @Bean
    fun provideRedisMessenger(provider: RedisProvider, settings: RedisSettings, json: Json, logger: Logger): RedisMessenger =
        RedisMessenger(LettucePubSubTransport(provider), settings, json, logger)
}
