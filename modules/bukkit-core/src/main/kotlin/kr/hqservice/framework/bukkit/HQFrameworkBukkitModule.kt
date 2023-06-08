package kr.hqservice.framework.bukkit

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kr.hqservice.framework.bukkit.core.serializers.ItemStackSerializer
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.plugin.PluginManager
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class HQFrameworkBukkitModule {
    @Single
    fun providePluginManager(): PluginManager {
        return Bukkit.getPluginManager()
    }

    @Single
    fun provideServer(): Server {
        return Bukkit.getServer()
    }

    @Single
    fun provideSerializationJson(): Json {
        return Json {
            serializersModule = SerializersModule {
                contextual(ItemStackSerializer)
            }
        }
    }
}