package kr.hqservice.framework.inventory.element

import kotlinx.coroutines.launch
import kr.hqservice.framework.inventory.coroutine.LifecycleOwner
import kr.hqservice.framework.inventory.event.ButtonInteractEvent
import kr.hqservice.framework.inventory.state.State
import org.bukkit.inventory.ItemStack

class ButtonElement(private val itemStack: ItemStack, private val lifecycleOwner: LifecycleOwner) : ViewElement {
    private var onClick: (ButtonInteractEvent) -> Unit = {}
    private var itemStackBuilder: ItemStack.() -> Unit = {}

    fun item(itemStackBuilderScope: ItemStack.() -> Unit) {
        itemStackBuilder = itemStackBuilderScope
        itemStackBuilderScope(this.itemStack)
    }

    fun onClick(onClick: (event: ButtonInteractEvent) -> Unit) {
        this.onClick = onClick
    }

    internal fun invokeOnclick(buttonInteractEvent: ButtonInteractEvent) {
        onClick.invoke(buttonInteractEvent)
    }

    override fun subscribe(vararg states: State<*>) {
        states.forEach { state ->
            lifecycleOwner.launch {
                state.getStateFlow().collect {
                    itemStackBuilder(this@ButtonElement.itemStack)
                }
            }
        }
    }
}