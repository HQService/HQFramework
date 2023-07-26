package kr.hqservice.framework.view

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.coroutine.extension.BukkitMain
import kr.hqservice.framework.view.coroutine.LifecycleOwner
import kr.hqservice.framework.view.element.ButtonElement
import kr.hqservice.framework.view.event.ButtonRenderEvent
import kr.hqservice.framework.view.scope.CloseScope
import kr.hqservice.framework.view.scope.CreateScope
import kr.hqservice.framework.view.scope.RenderScope
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class HQView(
    private val size: Int,
    private val title: String,
    val cancel: Boolean = true
) : InventoryLifecycle {
    private val job = Job()
    val _ownedLifecycles: MutableList<LifecycleOwner> = mutableListOf()
    private var baseInventory = lazy { Bukkit.createInventory(this@HQView, size, title.colorize()) }
    private val buttons: MutableMap<Int, ButtonElement> = mutableMapOf()
    internal val viewers: MutableList<UUID> = mutableListOf()

    override val coroutineContext: CoroutineContext
        get() = CoroutineName("HQViewCoroutine") + job + Dispatchers.Default

    protected abstract suspend fun CreateScope.onCreate()
    protected open suspend fun RenderScope.onRender(viewer: Player) {}
    protected open suspend fun CloseScope.onClose(viewer: Player) {}

    override fun registerButton(slot: Int, buttonElement: ButtonElement) {
        buttons[slot] = buttonElement
    }

    internal suspend fun open(vararg viewer: Player) = coroutineScope {
        viewer.map { player ->
            viewers.add(player.uniqueId)
            this.launch {
                CreateScope(this@HQView).onCreate()
                withContext(Dispatchers.BukkitMain) {
                    player.openInventory(inventory)
                }
                RenderScope(this@HQView, player).onRender(player)
                buttons.values.forEach { buttonElement ->
                    buttonElement.invokeOnRender(ButtonRenderEvent(this@HQView, buttonElement, player))
                }
            }
        }.joinAll()
    }

    final override fun getInventory(): Inventory {
        return baseInventory.value
    }

    fun getButton(index: Int): ButtonElement? {
        return buttons[index]
    }

    internal fun invokeOnClose(player: Player) {
        launch {
            CloseScope().onClose(player)
        }
    }

    final override fun dispose() {
        _ownedLifecycles.forEach { lifecycleOwner ->
            lifecycleOwner.dispose()
        }
        job.cancel()
    }
}