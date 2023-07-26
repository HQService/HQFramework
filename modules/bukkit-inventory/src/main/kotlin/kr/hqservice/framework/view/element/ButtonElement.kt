package kr.hqservice.framework.view.element

import kotlinx.coroutines.launch
import kr.hqservice.framework.view.InventoryLifecycle
import kr.hqservice.framework.view.event.ButtonInteractEvent
import kr.hqservice.framework.view.event.ButtonRenderEvent
import kr.hqservice.framework.view.state.State
import kr.hqservice.framework.view.state.SubscribableState
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ButtonElement(
    private val lifecycleOwner: InventoryLifecycle,
    private val index: Int,
) : ViewElement {
    private var onClick: (ButtonInteractEvent) -> Unit = {}
    private var onRender: (ButtonRenderEvent) -> Unit = {}
    private var itemStackBuilder: (Int) -> ItemStack? = { null }

    fun item(itemStackBuilderScope: (index: Int) -> ItemStack?) {
        itemStackBuilder = itemStackBuilderScope
    }

    fun item(material: Material, itemStackMetaScope: ItemStack.(index: Int) -> Unit = {}) {
        itemStackBuilder = {
            ItemStack(material).apply {
                itemStackMetaScope(this, index)
            }
        }
    }

    fun item(itemStack: ItemStack, itemStackMetaScope: ItemStack.(index: Int) -> Unit = {}) {
        itemStackBuilder = {
            itemStack.apply {
                itemStackMetaScope(this, index)
            }
        }
    }

    fun onClick(onClick: (event: ButtonInteractEvent) -> Unit) {
        this.onClick = onClick
    }

    fun onRender(onRender: (event: ButtonRenderEvent) -> Unit) {
        this.onRender = onRender
    }

    internal fun invokeOnclick(buttonInteractEvent: ButtonInteractEvent) {
        onClick.invoke(buttonInteractEvent)
    }

    internal fun invokeOnRender(buttonRenderEvent: ButtonRenderEvent) {
        onRender.invoke(buttonRenderEvent)
    }

    override fun subscribe(vararg states: State<*>) {
        states.forEach { state ->
            lifecycleOwner.launch {
                state as SubscribableState
                state.getStateFlow().collect {
                    lifecycleOwner.inventory.setItem(index, itemStackBuilder.invoke(index))
                }
            }
        }
    }
}