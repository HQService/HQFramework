package kr.hqservice.framework.nms.legacy.virtual.entity

import kr.hqservice.framework.nms.virtual.entity.display.HQBillboard

class LegacyHQBillboard(
    private val nmsInstance: Any
) : HQBillboard {
    override fun getUnwrappedInstance(): Any {
        return nmsInstance
    }
}