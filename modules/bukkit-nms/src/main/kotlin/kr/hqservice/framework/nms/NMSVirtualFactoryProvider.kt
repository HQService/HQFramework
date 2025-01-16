package kr.hqservice.framework.nms

import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboardFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.virtual.item.VirtualItemMessageFactory

interface NMSVirtualFactoryProvider {
    fun provideVirtualEntityClasses(): VirtualEntityClasses

    fun provideVirtualContainerMessageFactory(): VirtualContainerMessageFactory

    fun provideVirtualCameraFactory(): VirtualEntityFactory

    fun provideVirtualSignFactory(): VirtualEntityFactory

    fun provideVirtualAnvilHandlerFactory(): VirtualAnvilHandlerFactory

    fun provideVirtualItemHandlerFactory(): VirtualItemHandlerFactory

    fun provideVirtualSignHandlerFactory(): VirtualSignHandlerFactory

    fun provideVirtualItemMessageFactory(): VirtualItemMessageFactory

    fun provideHQBillboardFactory(): HQBillboardFactory
}