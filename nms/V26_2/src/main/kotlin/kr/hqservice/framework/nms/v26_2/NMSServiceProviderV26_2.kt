package kr.hqservice.framework.nms.v26_2

import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import kr.hqservice.framework.nms.v21_11.NMSServiceProviderImpl
import kr.hqservice.framework.nms.v21_11.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.v26_2.service.entity.NmsTextDisplayServiceImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.plugin.Plugin

class NMSServiceProviderV26_2(
    plugin: Plugin,
    languageRegistry: LanguageRegistry,
    virtualHandlerRegistry: VirtualHandlerRegistry,
    reflectionWrapper: NmsReflectionWrapperImpl
) : NMSServiceProviderImpl(plugin, languageRegistry, virtualHandlerRegistry, reflectionWrapper) {
    private val textDisplayService = NmsTextDisplayServiceImpl(super.provideBaseComponentService())

    override fun provideTextDisplayService(): NmsTextDisplayService {
        return textDisplayService
    }
}