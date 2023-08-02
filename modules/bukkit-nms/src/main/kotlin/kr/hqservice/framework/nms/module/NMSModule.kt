package kr.hqservice.framework.nms.module

import kotlinx.coroutines.cancel
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQModule
import kr.hqservice.framework.nms.registry.LanguageRegistry
import org.bukkit.plugin.Plugin
import org.koin.core.annotation.Named
import java.util.*

@Component
class NMSModule(
    private val plugin: Plugin,
    private val languageRegistry: LanguageRegistry,
    @Named("virtual") private val virtualScope: HQCoroutineScope
) : HQModule {
    override fun onEnable() {
        plugin.getResource("lang/ko_kr.json")
            ?.apply { languageRegistry.registerLanguage(this, Locale.KOREA) }
    }

    override fun onDisable() {
        virtualScope.cancel()
    }
}