package kr.hqservice.framework.view

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.coroutine.extension.BukkitMain
import kr.hqservice.framework.view.coroutine.LifecycleOwner
import kr.hqservice.framework.view.element.ButtonElement
import kr.hqservice.framework.view.element.TitleElement
import kr.hqservice.framework.view.event.ButtonRenderEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import kotlin.coroutines.CoroutineContext

abstract class HQView(
    private val size: Int,
    private val title: String,
    val cancel: Boolean = true
) : InventoryLifecycle {
    private val job = Job()
    val ownedLifecycles: MutableList<LifecycleOwner> = mutableListOf()
    private var baseInventory: Inventory? = null
    private val buttons: MutableMap<Int, ButtonElement> = mutableMapOf()

    override val coroutineContext: CoroutineContext
        get() = CoroutineName("HQViewCoroutine") + job + Dispatchers.BukkitMain

    protected abstract suspend fun CreateScope.onCreate()
    protected open suspend fun RenderScope.onRender(viewer: Player) {}
    protected open suspend fun CloseScope.onClose(viewer: Player) {}

    suspend fun open(vararg players: Player) = coroutineScope {
        players.map { player ->
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
        return baseInventory ?: Bukkit.createInventory(this, size, title.colorize()).apply {
            baseInventory = this
        }
    }

    fun getButton(index: Int): ButtonElement? {
        return buttons[index]
    }

    internal fun invokeOnClose(player: Player) {
        launch {
            CloseScope().onClose(player)
            if (this@HQView.inventory.viewers.isEmpty()) {
                this@HQView.dispose()
            }
        }
    }

    final override fun dispose() {
        ownedLifecycles.forEach { lifecycleOwner ->
            lifecycleOwner.dispose()
        }
        job.cancel()
    }
}

class CreateScope(inventoryLifecycle: InventoryLifecycle) : InventoryLifecycle by inventoryLifecycle, ButtonPlaceable

class RenderScope(inventoryLifecycle: InventoryLifecycle, private val player: Player) : InventoryLifecycle by inventoryLifecycle,
    ButtonPlaceable {
    fun title(title: String, titleScope: TitleElement.() -> Unit = {}) {
        titleScope(TitleElement(player, title, this).apply { setTitle() })
    }
}

class CloseScope

interface InventoryLifecycle : InventoryHolder, LifecycleOwner

interface ButtonPlaceable : InventoryLifecycle {
    fun button(itemStack: ItemStack, slot: Int, buttonScope: ButtonElement.() -> Unit = {}) {
        val clonedItemStack = itemStack.clone()
        val button = ButtonElement(clonedItemStack, this)
        buttonScope(button)
        this.inventory.setItem(slot, clonedItemStack)
    }

    fun button(material: Material, slot: Int, buttonScope: ButtonElement.() -> Unit = {}) {
        button(ItemStack(material), slot, buttonScope)
    }
}