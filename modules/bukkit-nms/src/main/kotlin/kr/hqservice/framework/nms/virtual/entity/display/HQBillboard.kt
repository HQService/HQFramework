package kr.hqservice.framework.nms.virtual.entity.display

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.koin.java.KoinJavaComponent.getKoin

interface HQBillboard : NmsWrapper {
    companion object {
        private val billboardFactory by getKoin().inject<HQBillboardFactory>()

        val FIXED = billboardFactory.create("FIXED")
        val VERTICAL = billboardFactory.create("VERTICAL")
        val HORIZONTAL = billboardFactory.create("HORIZONTAL")
        val CENTER = billboardFactory.create("CENTER")
    }
}