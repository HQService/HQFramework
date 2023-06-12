package kr.hqservice.framework.nms.wrapper.world

import kr.hqservice.framework.nms.wrapper.NmsWrapper

class WorldWrapper(
    private val world: Any
) : NmsWrapper {
    override fun getUnwrappedInstance(): Any {
        return world
    }
}