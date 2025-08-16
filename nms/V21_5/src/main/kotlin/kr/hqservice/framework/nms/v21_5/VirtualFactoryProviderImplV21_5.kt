package kr.hqservice.framework.nms.v21_5

import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.v21.virtual.entity.HQBillboardFactoryImpl
import kr.hqservice.framework.nms.v21.virtual.entity.VirtualCameraFactoryImpl
import kr.hqservice.framework.nms.v21.virtual.entity.VirtualSignFactoryImpl
import kr.hqservice.framework.nms.v21.virtual.handler.VirtualItemHandlerFactoryImpl
import kr.hqservice.framework.nms.v21.virtual.handler.VirtualSignHandlerFactoryImpl
import kr.hqservice.framework.nms.v21.virtual.item.VirtualItemMessageFactoryImpl
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.v21_5.virtual.classes.VirtualEntityClassesImpl
import kr.hqservice.framework.nms.v21_5.virtual.container.VirtualContainerMessageFactoryImpl
import kr.hqservice.framework.nms.v21_5.virtual.handler.VirtualAnvilHandlerFactoryImpl
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboardFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.virtual.item.VirtualItemMessageFactory

open class VirtualFactoryProviderImplV21_5(
    reflectionWrapper: NmsReflectionWrapperImpl,
    serviceProvider: NMSServiceProviderImplV21_5
) : NMSVirtualFactoryProvider {
    private val entityClasses = VirtualEntityClassesImpl(serviceProvider.provideBaseComponentService())
    private val virtualContainerMessageFactory = VirtualContainerMessageFactoryImpl(serviceProvider.provideBaseComponentService(), serviceProvider.provideContainerService())
    private val virtualCameraFactory = VirtualCameraFactoryImpl(reflectionWrapper)
    private val virtualSignFactory = VirtualSignFactoryImpl(serviceProvider.provideWorldService())
    private val virtualAnvilHandlerFactory = VirtualAnvilHandlerFactoryImpl()
    private val virtualItemHandlerFactory = VirtualItemHandlerFactoryImpl()
    private val virtualSignHandlerFactory = VirtualSignHandlerFactoryImpl()
    private val virtualItemMessageFactory = VirtualItemMessageFactoryImpl(
        serviceProvider.provideItemStackService(),
        serviceProvider.provideContainerService()
    )
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