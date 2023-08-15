package kr.hqservice.framework.bukkit.core.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kr.hqservice.framework.bukkit.core.serializers.ItemStackSerializer
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.component.Singleton

@Configuration
class SerializationConfig {
    @Singleton
    fun provideJson(): Json {
        return Json {
            serializersModule = SerializersModule {
                contextual(ItemStackSerializer)
            }
        }
    }
}