package kr.hqservice.framework.view

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.view.element.ButtonElement
import kr.hqservice.framework.view.event.ButtonRenderEvent
import kr.hqservice.framework.view.scope.CloseScope
import kr.hqservice.framework.view.scope.CreateScope
import kr.hqservice.framework.view.scope.RenderScope
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*

abstract class View(
    private val size: Int,
    private val title: String,
    val cancel: Boolean = true
) : InventoryHolder {
    private var baseInventory = lazy { Bukkit.createInventory(this@View, size, title.colorize()) }
    private val buttons: MutableMap<Int, ButtonElement> = mutableMapOf()
    internal val subscribes = mutableListOf<Job>()
    internal val viewerIds = mutableListOf<UUID>()
    val _childLifecycles = mutableListOf<LifecycleOwner>()
    protected val lifecycleJob = Job()

    protected abstract suspend fun CreateScope.onCreate()
    protected open fun RenderScope.onRender(viewer: Player) {}
    protected open fun CloseScope.onClose(viewer: Player) {}

    internal fun registerButton(slot: Int, buttonElement: ButtonElement) {
        buttons[slot] = buttonElement
    }

    internal suspend fun open(vararg viewer: Player, afterAction: suspend (player: Player) -> Unit) {
        coroutineScope {
            viewerIds.addAll(viewer.map { it.uniqueId })
            viewer.forEach { player ->
                launch(Dispatchers.IO) {
                    val createScope = CreateScope(this@View, this)
                    createScope.onCreate()
                    createScope.buttonJobs.joinAll()
                    withContext(Dispatchers.BukkitMain) {
                        player.openInventory(inventory)
                    }
                    RenderScope(this@View, this, player).onRender(player)
                    buttons.values.forEach { buttonElement ->
                        buttonElement.invokeOnRender(ButtonRenderEvent(this@View, buttonElement, player))
                    }
                    bukkitDelay(1)
                    afterAction(player)
                }
            }
        }
    }

    final override fun getInventory(): Inventory {
        return baseInventory.value
    }

    fun getButton(index: Int): ButtonElement? {
        return buttons[index]
    }

    internal fun invokeOnClose(player: Player) {
        CloseScope().onClose(player)
    }

    internal fun dispose() {
        subscribes.forEach { job ->
            job.cancel()
        }
        _childLifecycles.forEach { lifecycleOwner ->
            lifecycleOwner.dispose()
        }
        lifecycleJob.cancel()
    }
}