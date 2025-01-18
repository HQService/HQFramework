package kr.hqservice.framework.nms.legacy

import kr.hqservice.framework.nms.NMSServiceProvider
import kr.hqservice.framework.nms.legacy.service.chat.LegacyNmsBaseComponentService
import kr.hqservice.framework.nms.legacy.service.container.LegacyNMSContainerService
import kr.hqservice.framework.nms.legacy.service.entity.*
import kr.hqservice.framework.nms.legacy.service.item.LegacyNMSItemService
import kr.hqservice.framework.nms.legacy.service.item.LegacyNMSItemStackService
import kr.hqservice.framework.nms.legacy.service.item.LegacyNMSNBTTagCompoundService
import kr.hqservice.framework.nms.legacy.service.math.LegacyNmsVector3FService
import kr.hqservice.framework.nms.legacy.service.netty.LegacyNmsNettyInjectService
import kr.hqservice.framework.nms.legacy.service.world.LegacyNmsWorldBorderService
import kr.hqservice.framework.nms.legacy.service.world.LegacyWorldService
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.service.entity.NmsArmorStandService
import kr.hqservice.framework.nms.service.entity.NmsDisplayService
import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import kr.hqservice.framework.nms.service.item.NmsItemService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.service.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.service.math.NmsVector3fService
import kr.hqservice.framework.nms.service.world.NmsWorldBorderService
import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.util.NmsNettyInjectService
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.plugin.Plugin

class LegacyNMSServiceProvider(
    plugin: Plugin,
    languageRegistry: LanguageRegistry,
    virtualHandlerRegistry: VirtualHandlerRegistry,
    private val reflectionWrapper: LegacyNmsReflectionWrapper,
    private val supportDisplay: Boolean
) : NMSServiceProvider {
    private val baseComponentService = LegacyNmsBaseComponentService(reflectionWrapper)
    private val containerService = LegacyNMSContainerService(reflectionWrapper)
    private val vector3fService = LegacyNmsVector3FService(reflectionWrapper)
    private val worldService = LegacyWorldService(reflectionWrapper)
    private val worldBorderService = LegacyNmsWorldBorderService(reflectionWrapper, worldService)
    private val armorStandService = LegacyNMSArmorStandService(reflectionWrapper, vector3fService, worldService)
    private val nbtTagService = LegacyNMSNBTTagCompoundService(reflectionWrapper)
    private val itemService = LegacyNMSItemService(reflectionWrapper, languageRegistry)
    private val itemStackService = LegacyNMSItemStackService(nbtTagService, itemService)
    private val nettyInjectService = LegacyNmsNettyInjectService(plugin, reflectionWrapper, virtualHandlerRegistry)
    private val displayService = if (supportDisplay) LegacyNmsDisplayService(worldService, reflectionWrapper) else EmptyNmsDisplayService()
    private val textDisplayService = if (supportDisplay) LegacyNmsTextDisplayService(worldService, displayService, baseComponentService, reflectionWrapper) else EmptyNmsTextDisplayService()

    override fun provideBaseComponentService(): NmsBaseComponentService {
        return baseComponentService
    }

    override fun provideContainerService(): NmsContainerService {
        return containerService
    }

    override fun provideArmorStandService(): NmsArmorStandService {
        return armorStandService
    }

    override fun provideItemService(): NmsItemService {
        return itemService
    }

    override fun provideItemStackService(): NmsItemStackService {
        return itemStackService
    }

    override fun provideNBTTagService(): NmsNBTTagCompoundService {
        return nbtTagService
    }

    override fun provideVector3fService(): NmsVector3fService {
        return vector3fService
    }

    override fun provideWorldBorderService(): NmsWorldBorderService {
        return worldBorderService
    }

    override fun provideWorldService(): NmsWorldService {
        return worldService
    }

    override fun provideNettyInjectService(): NmsNettyInjectService {
        return nettyInjectService
    }

    override fun provideReflectionWrapperService(): NmsReflectionWrapper {
        return reflectionWrapper
    }

    override fun provideDisplayService(): NmsDisplayService {
        return displayService
    }

    override fun provideTextDisplayService(): NmsTextDisplayService {
        return textDisplayService
    }
}