package kr.hqservice.framework.nms.legacy.virtual.handler

import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import kr.hqservice.framework.nms.virtual.handler.AnvilDummyListener
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin

class LegacyAnvilDummyListener(
    private val player: Player,
    private val plugin: Plugin
) : AnvilDummyListener {
    override fun close()  {
        plugin.getScheduler().runTask {
            InventoryClickEvent.getHandlerList().unregister(this)
            player.updateInventory()
        }
    }
}