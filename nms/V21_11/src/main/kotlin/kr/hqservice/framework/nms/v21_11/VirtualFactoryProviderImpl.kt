package kr.hqservice.framework.nms.v21_11

import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.v21_11.virtual.classes.VirtualEntityClassesImpl
import kr.hqservice.framework.nms.v21_11.virtual.container.VirtualContainerMessageFactoryImpl
import kr.hqservice.framework.nms.v21_11.virtual.entity.HQBillboardFactoryImpl
import kr.hqservice.framework.nms.v21_11.virtual.entity.VirtualCameraFactoryImpl
import kr.hqservice.framework.nms.v21_11.virtual.entity.VirtualSignFactoryImpl
import kr.hqservice.framework.nms.v21_11.virtual.handler.VirtualAnvilHandlerFactoryImpl
import kr.hqservice.framework.nms.v21_11.virtual.handler.VirtualItemHandlerFactoryImpl
import kr.hqservice.framework.nms.v21_11.virtual.handler.VirtualSignHandlerFactoryImpl
import kr.hqservice.framework.nms.v21_11.virtual.item.VirtualItemMessageFactoryImpl
import kr.hqservice.framework.nms.v21_11.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboardFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.virtual.item.VirtualItemMessageFactory

open class VirtualFactoryProviderImpl(
    reflectionWrapper: NmsReflectionWrapperImpl,
    serviceProvider: NMSServiceProviderImpl
) : NMSVirtualFactoryProvider {
    private val entityClasses = VirtualEntityClassesImpl(serviceProvider.provideBaseComponentService())
    private val virtualContainerMessageFactory = VirtualContainerMessageFactoryImpl(serviceProvider.provideBaseComponentService(), serviceProvider.provideContainerService())
    private val virtualCameraFactory = VirtualCameraFactoryImpl(reflectionWrapper)
    private val virtualSignFactory = VirtualSignFactoryImpl(serviceProvider.provideWorldService())
    private val virtualAnvilHandlerFactory = VirtualAnvilHandlerFactoryImpl()
    private val virtualItemHandlerFactory = VirtualItemHandlerFactoryImpl()
    private val virtualSignHandlerFactory = VirtualSignHandlerFactoryImpl()
    private val virtualItemMessageFactory = VirtualItemMessageFactoryImpl(serviceProvider.provideItemStackService(), serviceProvider.provideContainerService())
    private val hqBillboardFactory = HQBillboardFactoryImpl()

    override fun provideVirtualEntityClasses(): VirtualEntityClasses {
        return entityClasses
    }

    override fun provideVirtualContainerMessageFactory(): VirtualContainerMessageFactory {
        return virtualContainerMessageFactory
    }

    override fun provideVirtualCameraFactory(): VirtualEntityFactory {
        return virtualCameraFactory
    }

    override fun provideVirtualSignFactory(): VirtualEntityFactory {
        return virtualSignFactory
    }

    override fun provideVirtualAnvilHandlerFactory(): VirtualAnvilHandlerFactory {
        return virtualAnvilHandlerFactory
    }

    override fun provideVirtualItemHandlerFactory(): VirtualItemHandlerFactory {
        return virtualItemHandlerFactory
    }

    override fun provideVirtualSignHandlerFactory(): VirtualSignHandlerFactory {
        return virtualSignHandlerFactory
    }

    override fun provideVirtualItemMessageFactory(): VirtualItemMessageFactory {
        return virtualItemMessageFactory
    }

    override fun provideHQBillboardFactory(): HQBillboardFactory {
        return hqBillboardFactory
    }
}