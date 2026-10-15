package kr.hqservice.framework.nms.v21_5

import kr.hqservice.framework.nms.v21.NMSServiceProviderImpl
import kr.hqservice.framework.nms.v21.VirtualFactoryProviderImpl
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsEntityPlayerAccessor
import kr.hqservice.framework.nms.v21_5.virtual.classes.VirtualEntityClassesImpl
import kr.hqservice.framework.nms.v21_5.virtual.handler.VirtualAnvilHandlerFactoryImpl
import kr.hqservice.framework.nms.v21_5.virtual.handler.VirtualItemHandlerFactoryImpl
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory

open class VirtualFactoryProviderImplV21_5(
    reflectionWrapper: NmsEntityPlayerAccessor,
    serviceProvider: NMSServiceProviderImpl
) : VirtualFactoryProviderImpl(reflectionWrapper, serviceProvider) {
    private val entityClasses = VirtualEntityClassesImpl(serviceProvider.provideBaseComponentService())
    private val virtualAnvilHandlerFactory = VirtualAnvilHandlerFactoryImpl()
    private val virtualItemHandlerFactory = VirtualItemHandlerFactoryImpl()

    override fun provideVirtualEntityClasses(): VirtualEntityClasses {
        return entityClasses
    }

    override fun provideVirtualAnvilHandlerFactory(): VirtualAnvilHandlerFactory {
        return virtualAnvilHandlerFactory
    }

    override fun provideVirtualItemHandlerFactory(): VirtualItemHandlerFactory {
        return virtualItemHandlerFactory
    }
}
