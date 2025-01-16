package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper

abstract class NmsItemWrapper : NmsWrapper {
    abstract fun getDescriptionName(): String

    abstract fun getLocalizedName(): String?
}