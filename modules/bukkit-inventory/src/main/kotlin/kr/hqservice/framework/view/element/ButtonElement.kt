package kr.hqservice.framework.view.element

import kr.hqservice.framework.view.event.ButtonInteractEvent
import kr.hqservice.framework.view.event.ButtonRenderEvent
import kr.hqservice.framework.view.state.State
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ButtonElement(
    internal val index: Int
) : ViewElement {
    private var onClick: suspend (ButtonInteractEvent) -> Unit = {}
    private var onRender: (ButtonRenderEvent) -> Unit = {}
    internal var itemStackBuilder: suspend (Int) -> ItemStack? = { null }
    internal var subscribedStates: MutableList<State<*>> = mutableListOf()

    fun item(itemStackBuilderScope: suspend (index: Int) -> ItemStack?) {
        itemStackBuilder = itemStackBuilderScope
    }

    fun item(material: Material, itemStackMetaScope: suspend ItemStack.(index: Int) -> Unit = {}) {
        itemStackBuilder = {
            ItemStack(material).apply {
                itemStackMetaScope(this, index)
            }
        }
    }

    fun item(itemStack: ItemStack, itemStackMetaScope: suspend ItemStack.(index: Int) -> Unit = {}) {
        itemStackBuilder = {
            itemStack.apply {
                itemStackMetaScope(this, index)
            }
        }
    }

    fun onClick(onClick: suspend (event: ButtonInteractEvent) -> Unit) {
        this.onClick = onClick
    }

    fun onRender(onRender: (event: ButtonRenderEvent) -> Unit) {
        this.onRender = onRender
    }

    internal suspend fun invokeOnclick(buttonInteractEvent: ButtonInteractEvent) {
        onClick.invoke(buttonInteractEvent)
    }

    internal fun invokeOnRender(buttonRenderEvent: ButtonRenderEvent) {
        onRender.invoke(buttonRenderEvent)
    }

    override fun subscribe(vararg states: State<*>) {
        states.forEach {
            subscribedStates.add(it)
        }
    }
}