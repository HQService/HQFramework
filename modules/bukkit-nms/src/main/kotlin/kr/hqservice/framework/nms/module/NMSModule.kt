package kr.hqservice.framework.nms.module

import kotlinx.coroutines.cancel
import kr.hqservice.framework.bukkit.core.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.nms.registry.LanguageRegistry
import org.bukkit.plugin.Plugin
import java.util.*

@Component
class NMSModule(
    private val plugin: Plugin,
    private val languageRegistry: LanguageRegistry,
    @Qualifier("virtual") private val virtualScope: HQCoroutineScope
) : HQModule {
    override fun onEnable() {
        plugin.getResource("lang/ko_kr.json")
            ?.apply { languageRegistry.registerLanguage(this, Locale.KOREA) }
    }

    override fun onDisable() {
        virtualScope.cancel()
    }
}