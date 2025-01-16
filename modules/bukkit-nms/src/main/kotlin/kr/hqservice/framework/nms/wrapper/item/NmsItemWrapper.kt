package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper

interface NmsItemWrapper : NmsWrapper {
    fun getDescriptionName(): String

    fun getLocalizedName(): String?
}