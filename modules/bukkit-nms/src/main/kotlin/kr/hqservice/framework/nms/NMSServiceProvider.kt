package kr.hqservice.framework.nms

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
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper

interface NMSServiceProvider {
    fun provideBaseComponentService(): NmsBaseComponentService

    fun provideContainerService(): NmsContainerService

    fun provideArmorStandService(): NmsArmorStandService

    fun provideItemService(): NmsItemService

    fun provideItemStackService(): NmsItemStackService

    fun provideNBTTagService(): NmsNBTTagCompoundService

    fun provideVector3fService(): NmsVector3fService

    fun provideWorldBorderService(): NmsWorldBorderService

    fun provideWorldService(): NmsWorldService

    fun provideNettyInjectService(): NmsNettyInjectService

    fun provideReflectionWrapperService(): NmsReflectionWrapper
}