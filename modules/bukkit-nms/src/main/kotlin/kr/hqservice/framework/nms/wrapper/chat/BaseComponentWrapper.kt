package kr.hqservice.framework.nms.wrapper.chat

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import net.minecraft.network.chat.Component

class BaseComponentWrapper(
    val baseString: String,
    private val baseComponent: Component
) : NmsWrapper {
    override fun getUnwrappedInstance(): Any {
        return baseComponent
    }
}