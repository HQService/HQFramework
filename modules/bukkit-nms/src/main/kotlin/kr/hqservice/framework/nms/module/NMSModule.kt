package kr.hqservice.framework.nms.module

import kotlinx.coroutines.cancel
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.nms.registry.LanguageRegistry
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.*

@Module
class NMSModule(
    private val plugin: Plugin,
    private val languageRegistry: LanguageRegistry,
    @Qualifier("virtual") private val virtualScope: HQCoroutineScope
) {
    @Setup
    fun setup() {
        val path = "lang/ko_kr.json"
        if (!File(plugin.dataFolder, path).exists()) {
            plugin.saveResource(path, false)
        }
        val folder = File(plugin.dataFolder, "lang")
        folder.listFiles()?.forEach {
            languageRegistry.registerLanguage(it.inputStream(), Locale(it.name.removeSuffix(".json")))
        }
    }

    @Teardown
    fun teardown() {
        virtualScope.cancel()
    }
}