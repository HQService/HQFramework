package kr.hqservice.framework.view.element

import kotlinx.coroutines.launch
import kr.hqservice.framework.view.coroutine.LifecycleOwner
import kr.hqservice.framework.view.event.ButtonInteractEvent
import kr.hqservice.framework.view.event.ButtonRenderEvent
import kr.hqservice.framework.view.state.State
import kr.hqservice.framework.view.state.SubscribableState
import org.bukkit.inventory.ItemStack

class ButtonElement(private val itemStack: ItemStack, private val lifecycleOwner: LifecycleOwner) : ViewElement {
    private var onClick: (ButtonInteractEvent) -> Unit = {}
    private var onRender: (ButtonRenderEvent) -> Unit = {}
    private var itemStackBuilder: ItemStack.() -> Unit = {}

    fun item(itemStackBuilderScope: ItemStack.() -> Unit) {
        itemStackBuilder = itemStackBuilderScope
        itemStackBuilderScope(this.itemStack)
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
                    itemStackBuilder(this@ButtonElement.itemStack)
                }
            }
        }
    }
}