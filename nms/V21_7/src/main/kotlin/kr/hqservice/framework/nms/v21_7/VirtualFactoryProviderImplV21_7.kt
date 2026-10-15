package kr.hqservice.framework.nms.v21_7

import kr.hqservice.framework.nms.v21.NMSServiceProviderImpl
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsEntityPlayerAccessor
import kr.hqservice.framework.nms.v21_5.VirtualFactoryProviderImplV21_5
import kr.hqservice.framework.nms.v21_7.virtual.container.VirtualContainerMessageFactoryImpl
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory

class VirtualFactoryProviderImplV21_7(
    reflectionWrapper: NmsEntityPlayerAccessor,
    serviceProvider: NMSServiceProviderImpl
) : VirtualFactoryProviderImplV21_5(reflectionWrapper, serviceProvider) {
    private val virtualContainerMessageFactory = VirtualContainerMessageFactoryImpl(
        serviceProvider.provideBaseComponentService(),
        serviceProvider.provideContainerService()
    )

    override fun provideVirtualContainerMessageFactory(): VirtualContainerMessageFactory {
        return virtualContainerMessageFactory
    }
}
