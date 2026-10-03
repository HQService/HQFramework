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
    fun providePubSubTransport(provider: RedisProvider, logger: Logger): PubSubTransport = LettucePubSubTransport(provider, logger)

    @Bean
    fun provideRedisMessenger(transport: PubSubTransport, settings: RedisSettings, json: Json, logger: Logger): RedisMessenger =
        RedisMessenger(transport, settings, json, logger)

    @Bean
    fun provideRedisStores(provider: RedisProvider, json: Json): RedisStores = RedisStores(provider, json)
}
