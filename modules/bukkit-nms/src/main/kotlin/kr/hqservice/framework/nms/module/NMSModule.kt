package kr.hqservice.framework.nms.module

import kotlinx.coroutines.cancel
import kr.hqservice.framework.bukkit.core.coroutine.component.coroutinescope.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.nms.registry.LanguageRegistry
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.*

@Component
class NMSModule(
    private val plugin: Plugin,
    private val languageRegistry: LanguageRegistry,
    @Qualifier("virtual") private val virtualScope: HQCoroutineScope
) : HQModule {
    override fun onEnable() {
        plugin.saveResource("lang/ko_kr.json", false)

        val folder = File(plugin.dataFolder, "lang")
        folder.listFiles()?.forEach {
            languageRegistry.registerLanguage(it.inputStream(), Locale(it.name.removeSuffix(".json")))
        }
    }

    override fun onDisable() {
        virtualScope.cancel()
    }
}