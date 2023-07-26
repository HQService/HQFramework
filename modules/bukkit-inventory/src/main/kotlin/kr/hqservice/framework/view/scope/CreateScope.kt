package kr.hqservice.framework.view.scope

import kotlinx.coroutines.launch
import kr.hqservice.framework.view.InventoryLifecycle
import kr.hqservice.framework.view.element.ButtonElement
import kr.hqservice.framework.view.state.SubscribableState

class CreateScope(inventoryLifecycle: InventoryLifecycle) : InventoryLifecycle by inventoryLifecycle {
    suspend fun button(vararg slots: Int, buttonScope: suspend ButtonElement.() -> Unit) {
        slots.forEach { slot ->
            val button = ButtonElement(this, slot)
            registerButton(slot, button)
            buttonScope(button)
            this.inventory.setItem(button.index, button.itemStackBuilder.invoke(button.index))
            button.subscribedStates.forEach { state ->
                launch {
                    state as SubscribableState
                    state.getStateFlow().collect {
                        this@CreateScope.inventory.setItem(button.index, button.itemStackBuilder.invoke(button.index))
                    }
                }
            }
        }
    }

    suspend fun button(intRange: IntRange, buttonScope: suspend ButtonElement.() -> Unit) {
        this.button(slots = intRange.toList().toTypedArray().toIntArray(), buttonScope)
    }
}