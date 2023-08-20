package kr.hqservice.framework.nms.wrapper.world

import kr.hqservice.framework.nms.wrapper.NmsWrapper

class WorldBorderWrapper(
    private val worldBorder: Any,
) : NmsWrapper {
    override fun getUnwrappedInstance(): Any {
        return worldBorder
    }
}