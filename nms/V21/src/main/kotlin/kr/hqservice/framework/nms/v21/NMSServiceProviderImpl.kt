package kr.hqservice.framework.nms.v21

import kr.hqservice.framework.nms.NMSServiceProvider
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.service.entity.NmsArmorStandService
import kr.hqservice.framework.nms.service.item.NmsItemService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.service.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.service.math.NmsVector3fService
import kr.hqservice.framework.nms.service.world.NmsWorldBorderService
import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.util.NmsNettyInjectService
import kr.hqservice.framework.nms.v21.service.chat.NmsBaseComponentServiceImpl
import kr.hqservice.framework.nms.v21.service.container.NmsContainerServiceImpl
import kr.hqservice.framework.nms.v21.service.entity.NmsArmorStandServiceImpl
import kr.hqservice.framework.nms.v21.service.item.NmsItemServiceImpl
import kr.hqservice.framework.nms.v21.service.item.NmsItemStackServiceImpl
import kr.hqservice.framework.nms.v21.service.item.NmsNBTTagCompoundServiceImpl
import kr.hqservice.framework.nms.v21.service.math.NmsVector3fServiceImpl
import kr.hqservice.framework.nms.v21.service.netty.NmsNettyInjectServiceImpl
import kr.hqservice.framework.nms.v21.service.world.NmsWorldBoardServiceImpl
import kr.hqservice.framework.nms.v21.service.world.NmsWorldServiceImpl
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.plugin.Plugin

open class NMSServiceProviderImpl(
    plugin: Plugin,
    languageRegistry: LanguageRegistry,
    virtualHandlerRegistry: VirtualHandlerRegistry,
    private val reflectionWrapper: NmsReflectionWrapperImpl
) : NMSServiceProvider {
    private val baseComponentService = NmsBaseComponentServiceImpl()
    private val containerService = NmsContainerServiceImpl(reflectionWrapper)
    private val vector3fService = NmsVector3fServiceImpl()
    private val worldService = NmsWorldServiceImpl()
    private val worldBorderService = NmsWorldBoardServiceImpl(worldService)
    private val armorStandService = NmsArmorStandServiceImpl(vector3fService)
    private val nbtTagService = NmsNBTTagCompoundServiceImpl()
    private val itemService = NmsItemServiceImpl(languageRegistry)
    private val itemStackService = NmsItemStackServiceImpl(languageRegistry)
    private val nettyInjectService = NmsNettyInjectServiceImpl(plugin, reflectionWrapper, virtualHandlerRegistry)

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
}