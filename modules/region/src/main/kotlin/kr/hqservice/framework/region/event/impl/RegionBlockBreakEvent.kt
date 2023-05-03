package kr.hqservice.framework.region.event.impl

import kr.hqservice.framework.region.data.Region
import kr.hqservice.framework.region.event.RegionBlockEvent
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class RegionBlockBreakEvent(
    private val region: Region,
    private val block: Block,
    private val player: Player
) : RegionBlockEvent(block), Cancellable {

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList() = handlers
    }

    private var cancel = false

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }
}