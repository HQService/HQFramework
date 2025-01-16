package kr.hqservice.framework.nms.virtual.entity.display

interface HQBillboardFactory {
    fun create(nmsName: String): HQBillboard
}