package kr.hqservice.framework.nms.wrapper.chat

import kr.hqservice.framework.nms.wrapper.NmsWrapper

class BaseComponentWrapper(
    val baseString: String,
    private val baseComponent: Any
) : NmsWrapper {
    override fun getUnwrappedInstance(): Any {
        return baseComponent
    }
}