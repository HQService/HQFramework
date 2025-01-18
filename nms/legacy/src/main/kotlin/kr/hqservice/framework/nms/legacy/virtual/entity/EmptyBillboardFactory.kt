package kr.hqservice.framework.nms.legacy.virtual.entity

import kr.hqservice.framework.nms.virtual.entity.display.HQBillboard
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboardFactory

class EmptyBillboardFactory : HQBillboardFactory {
    override fun create(nmsName: String): HQBillboard {
        throw UnsupportedOperationException("Not supported")
    }
}