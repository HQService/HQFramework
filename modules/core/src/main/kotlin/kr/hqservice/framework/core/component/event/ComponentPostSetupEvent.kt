package kr.hqservice.framework.core.component.event

import kr.hqservice.framework.core.component.HQComponent
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * ComponentPostSetupEvent 는 모든 Component 들이 setup 이 완료된 후에
 * Component 들과 함께 한번 호출됩니다.
 */
class ComponentPostSetupEvent(val componentHandlers: List<HQComponent>) : Event() {
    companion object {
        private var handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }
}