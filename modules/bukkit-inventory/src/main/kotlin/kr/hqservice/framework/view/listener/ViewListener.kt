package kr.hqservice.framework.view.listener

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.event.ButtonInteractEvent
import kr.hqservice.framework.view.navigator.Navigator
import kr.hqservice.framework.view.navigator.impl.NavigatorImpl
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.InventoryView

@Listener
class ViewListener(private val navigator: Navigator) {
    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClick(event: InventoryClickEvent) {
        val view = getView(event.view) ?: return
        val button = view.getButton(event.rawSlot)
        event.isCancelled = view.cancel || button != null
        val plugin = PluginScopeFinder.get(view::class)
        val clickJob = plugin.launch(Dispatchers.BukkitMain) {
            if (event.clickedInventory == event.whoClicked.inventory) {
                view.invokeOnClickBottom(event)
            } else if (event.clickedInventory != null) {
                view.invokeOnClickTop(event)
            }
        }
        if (button != null) {
            plugin.launch(Dispatchers.BukkitMain) {
                clickJob.join()
                button.invokeOnclick(ButtonInteractEvent(view, button, event))
            }
        }
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryDrag(event: InventoryDragEvent) {
        val view = getView(event.view) ?: return
        val topInventorySize = event.view.topInventory.size
        val isButtonDragged = event.rawSlots.any { rawSlot -> rawSlot < topInventorySize && view.getButton(rawSlot) != null }
        if (view.cancel || isButtonDragged) event.isCancelled = true
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClose(event: InventoryCloseEvent) {
        val view = getView(event.view)
        val player = event.player as Player
        if (navigator !is NavigatorImpl) {
            return
        }
        if (view != null && !navigator.isAllowToChangeView(player.uniqueId) && navigator.current(player.uniqueId) === view) {
            view.invokeOnClose(player)
            val plugin = PluginScopeFinder.get(view::class)
            plugin.launch(Dispatchers.IO) {
                navigator.goPrevious(player)
            }
        }
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun playerQuit(event: PlayerQuitEvent) {
        if (navigator !is NavigatorImpl) {
            return
        }
        navigator.clear(event.player)
    }

    private fun getView(inventoryView: InventoryView): View? {
        val holder = inventoryView.topInventory.holder ?: return null
        return if (holder is View) holder else null
    }
}
