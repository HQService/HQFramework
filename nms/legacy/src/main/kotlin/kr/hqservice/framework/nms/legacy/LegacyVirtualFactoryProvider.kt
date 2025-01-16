package kr.hqservice.framework.nms.legacy

import kr.hqservice.framework.nms.NMSVirtualFactoryProvider
import kr.hqservice.framework.nms.legacy.virtual.classes.LegacyVirtualEntityClasses
import kr.hqservice.framework.nms.legacy.virtual.container.LegacyVirtualContainerMessageFactory
import kr.hqservice.framework.nms.legacy.virtual.entity.LegacyVirtualCameraFactory
import kr.hqservice.framework.nms.legacy.virtual.entity.LegacyVirtualSignFactory
import kr.hqservice.framework.nms.legacy.virtual.handler.LegacyVirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.legacy.virtual.handler.LegacyVirtualItemHandlerFactory
import kr.hqservice.framework.nms.legacy.virtual.handler.LegacyVirtualSignHandlerFactory
import kr.hqservice.framework.nms.legacy.virtual.item.LegacyVirtualItemMessageFactory
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.virtual.item.VirtualItemMessageFactory

class LegacyVirtualFactoryProvider(
    reflectionWrapper: LegacyNmsReflectionWrapper,
    serviceProvider: LegacyNMSServiceProvider
) : NMSVirtualFactoryProvider {
    private val entityClasses = LegacyVirtualEntityClasses(reflectionWrapper, serviceProvider.provideBaseComponentService())
    private val virtualContainerMessageFactory = LegacyVirtualContainerMessageFactory(reflectionWrapper, serviceProvider.provideBaseComponentService(), serviceProvider.provideContainerService())
    private val virtualCameraFactory = LegacyVirtualCameraFactory(reflectionWrapper)
    private val virtualSignFactory = LegacyVirtualSignFactory(serviceProvider.provideWorldService(), reflectionWrapper)
    private val virtualAnvilHandlerFactory = LegacyVirtualAnvilHandlerFactory()
    private val virtualItemHandlerFactory = LegacyVirtualItemHandlerFactory()
    private val virtualSignHandlerFactory = LegacyVirtualSignHandlerFactory()
    private val virtualItemMessageFactory = LegacyVirtualItemMessageFactory(reflectionWrapper, serviceProvider.provideItemStackService(), serviceProvider.provideContainerService())

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
}