package kr.hqservice.framework.view.handler

import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.view.HQView
import kr.hqservice.framework.view.event.ButtonInteractEvent
import kr.hqservice.framework.view.navigator.Navigator
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryView

@Listener
class HQViewHandler(private val navigator: Navigator) {
    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClick(event: InventoryClickEvent) {
        getView(event.view)?.apply {
            event.isCancelled = this.cancel
            val button = getButton(event.rawSlot)
            if (button != null) {
                event.isCancelled = true
                button.invokeOnclick(ButtonInteractEvent(this, button, event))
            }
        }
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClose(event: InventoryCloseEvent) {
        val view = getView(event.view)
        val player = event.player
        if (view != null && player is Player) {
            view.invokeOnClose(player)
            view.launch {
                navigator.goPrevious(player)
            }
            var viewQuited = 0
            for(viewerId in view.viewers) {
                if (navigator.openedViews(viewerId).filterIsInstance(this::class.java).isEmpty()) {
                    viewQuited++
                }
            }
            if (viewQuited == view.viewers.size) {
                view.dispose()
                println("disposed")
            }
        }
    }

    private fun getView(inventoryView: InventoryView): HQView? {
        val holder = inventoryView.topInventory.holder ?: return null
        return if (holder is HQView) holder else null
    }
}