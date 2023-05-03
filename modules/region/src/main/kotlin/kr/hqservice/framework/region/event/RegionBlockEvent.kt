package kr.hqservice.framework.region.event

import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class RegionBlockEvent(val targetBlock: Block) : Event() {

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }
}