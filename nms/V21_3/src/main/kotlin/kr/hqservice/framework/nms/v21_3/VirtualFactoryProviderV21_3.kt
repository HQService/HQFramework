package kr.hqservice.framework.nms.v21_3

import kr.hqservice.framework.nms.v21.VirtualFactoryProviderImpl
import kr.hqservice.framework.nms.v21_3.virtual.classes.VirtualEntityClassesImpl
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses

class VirtualFactoryProviderV21_3(
    reflectionWrapper: NmsReflectionWrapperImpl,
    serviceProvider: NMSServiceProviderV21_3
) : VirtualFactoryProviderImpl(reflectionWrapper, serviceProvider) {
    private val entityClasses = VirtualEntityClassesImpl(serviceProvider.provideBaseComponentService())

    override fun provideVirtualEntityClasses(): VirtualEntityClasses {
        return entityClasses
    }
}