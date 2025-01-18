package kr.hqservice.framework.nms.virtual.entity.display

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.koin.java.KoinJavaComponent.getKoin

interface HQBillboard : NmsWrapper {
    companion object {
        private val billboardFactory by getKoin().inject<HQBillboardFactory>()

        val FIXED by lazy { billboardFactory.create("FIXED") }
        val VERTICAL by lazy { billboardFactory.create("VERTICAL") }
        val HORIZONTAL by lazy { billboardFactory.create("HORIZONTAL") }
        val CENTER  by lazy { billboardFactory.create("CENTER") }
    }
}