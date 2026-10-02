package kr.hqservice.framework.view

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.view.element.ButtonElement
import kr.hqservice.framework.view.event.ButtonRenderEvent
import kr.hqservice.framework.view.scope.ClickScope
import kr.hqservice.framework.view.scope.CloseScope
import kr.hqservice.framework.view.scope.CreateScope
import kr.hqservice.framework.view.scope.RenderScope
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

abstract class View(
    private val size: Int,
    private val title: String,
    val cancel: Boolean = true
) : InventoryHolder {
    private var baseInventory = lazy { Bukkit.createInventory(this@View, size, title.colorize()) }
    private val buttons: MutableMap<Int, ButtonElement> = mutableMapOf()
    internal val subscribes: MutableList<Job> = CopyOnWriteArrayList()
    internal val viewerIds: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val _childLifecycles: MutableList<LifecycleOwner> = CopyOnWriteArrayList()
    val childLifecycles: List<LifecycleOwner> get() = _childLifecycles
    private val isCreated = AtomicBoolean(false)
    protected val lifecycleJob = Job()

    protected abstract suspend fun CreateScope.onCreate()
    protected open fun RenderScope.onRender(viewer: Player) {}
    protected open fun CloseScope.onClose(viewer: Player) {}
    protected open suspend fun ClickScope.onClickTop(clicker: Player) {}
    protected open suspend fun ClickScope.onClickBottom(clicker: Player) {}

    internal fun addChildLifecycle(lifecycleOwner: LifecycleOwner) {
        _childLifecycles.add(lifecycleOwner)
    }

    internal fun registerButton(slot: Int, buttonElement: ButtonElement) {
        buttons[slot] = buttonElement
    }

    internal suspend fun open(vararg viewer: Player, afterAction: suspend (player: Player) -> Unit) {
        coroutineScope {
            viewerIds.addAll(viewer.map { it.uniqueId })
            if (isCreated.compareAndSet(false, true)) {
                try {
                    val openContext = Dispatchers.IO + CoroutineName("HQFrameworkViewOpenCoroutine")
                    val createScope = CreateScope(this@View, this + openContext)
                    withContext(openContext) {
                        createScope.onCreate()
                    }
                    createScope.buttonJobs.joinAll()
                } catch (throwable: Throwable) {
                    isCreated.set(false)
                    throw throwable
                }
            }
            viewer.forEach { player ->
                launch(Dispatchers.IO + CoroutineName("HQFrameworkViewOpenCoroutine")) {
                    withContext(Dispatchers.BukkitMain) {
                        player.openInventory(inventory)
                    }
                    RenderScope(this@View, this, player).onRender(player)
                    buttons.values.forEach { buttonElement ->
                        buttonElement.invokeOnRender(ButtonRenderEvent(this@View, buttonElement, player))
                    }
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

    internal suspend fun invokeOnClickTop(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        ClickScope(event).onClickTop(player)
    }

    internal suspend fun invokeOnClickBottom(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        ClickScope(event).onClickBottom(player)
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