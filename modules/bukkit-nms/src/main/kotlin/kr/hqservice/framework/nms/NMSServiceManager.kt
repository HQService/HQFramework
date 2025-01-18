package kr.hqservice.framework.nms

import kr.hqservice.framework.global.core.component.HQSimpleComponent

interface NMSServiceManager : HQSimpleComponent {
    companion object {
        internal var instance: NMSServiceManager? = null
    }

    fun support(version: Version): Boolean

    fun initialize()

    fun getServiceProvider(): NMSServiceProvider

    fun getVirtualFactoryProvider(): NMSVirtualFactoryProvider
}