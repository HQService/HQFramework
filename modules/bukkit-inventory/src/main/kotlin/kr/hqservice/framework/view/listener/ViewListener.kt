package kr.hqservice.framework.view.listener

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import org.bukkit.inventory.InventoryView

@Listener
class ViewListener(private val navigator: Navigator) {
    @OptIn(ExperimentalStdlibApi::class)
    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClick(event: InventoryClickEvent) {
        getView(event.view)?.apply view@{
            event.isCancelled = this.cancel
            runBlocking(PluginScopeFinder.get(this::class).coroutineContext.minusKey(CoroutineDispatcher.Key)) {
                if (event.clickedInventory == event.whoClicked.inventory) {
                    this@view.invokeOnClickBottom(event)
                } else if (event.clickedInventory != null && event.clickedInventory != event.whoClicked.inventory) {
                    this@view.invokeOnClickTop(event)
                }
                val button = getButton(event.rawSlot)
                if (button != null) {
                    event.isCancelled = true
                    button.invokeOnclick(ButtonInteractEvent(this@view, button, event))
                }
            }
        }
    }

    @Subscribe(handleOrder = HandleOrder.FIRST)
    fun inventoryClose(event: InventoryCloseEvent) {
        val view = getView(event.view)
        val player = event.player as Player
        navigator as NavigatorImpl
        if (view != null && !navigator.isAllowToChangeView(player.uniqueId)) {
            view.invokeOnClose(player)
            val plugin = PluginScopeFinder.get(view::class)
            plugin.launch(Dispatchers.IO) {
                navigator.goPrevious(player)

                var viewQuited = 0
                for (viewerId in view.viewerIds) {
                    if (navigator.openedViews(viewerId).filterIsInstance(view::class.java).isEmpty()) {
                        viewQuited++
                    }
                }
                if (viewQuited == view.viewerIds.size) {
                    view.dispose()
                }
            }
        }
    }

    private fun getView(inventoryView: InventoryView): View? {
        val holder = inventoryView.topInventory.holder ?: return null
        return if (holder is View) holder else null
    }
}