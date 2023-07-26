package kr.hqservice.framework.view.scope

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.hqservice.framework.view.InventoryLifecycle
import kr.hqservice.framework.view.element.ButtonElement
import kr.hqservice.framework.view.state.SubscribableState

class CreateScope(inventoryLifecycle: InventoryLifecycle) : InventoryLifecycle by inventoryLifecycle {
    internal val buttonJobs: MutableList<Job> = mutableListOf()

    fun button(vararg slots: Int, buttonScope: ButtonElement.() -> Unit) {
        slots.forEach { slot ->
            val button = ButtonElement(this@CreateScope, slot)
            registerButton(slot, button)
            buttonScope(button)
            val buttonJob = launch {
                val buttonItemStack = button.itemStackBuilder.invoke(button.index)
                this@CreateScope.inventory.setItem(button.index, buttonItemStack)
            }
            buttonJobs.add(buttonJob)
            button.subscribedStates.forEach { state ->
                launch {
                    buttonJob.join()
                    state as SubscribableState
                    state.getStateFlow().collect {
                        this@CreateScope.inventory.setItem(button.index, button.itemStackBuilder.invoke(button.index))
                    }
                }
            }
        }
    }

    fun button(intRange: IntRange, buttonScope: ButtonElement.() -> Unit) {
        this.button(slots = intRange.toList().toTypedArray().toIntArray(), buttonScope)
    }
}