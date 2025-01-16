package kr.hqservice.framework.nms.v21_3

import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.v21.NMSServiceProviderImpl
import kr.hqservice.framework.nms.v21_3.service.entity.NmsArmorStandServiceImpl
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import org.bukkit.plugin.Plugin

class NMSServiceProviderV21_3(
    plugin: Plugin,
    languageRegistry: LanguageRegistry,
    virtualHandlerRegistry: VirtualHandlerRegistry,
    reflectionWrapper: NmsReflectionWrapperImpl
) : NMSServiceProviderImpl(plugin, languageRegistry, virtualHandlerRegistry, reflectionWrapper) {
    private val armorStandServiceImpl = NmsArmorStandServiceImpl(super.provideVector3fService())

    override fun provideArmorStandService(): NmsArmorStandServiceImpl {
        return armorStandServiceImpl
    }
}