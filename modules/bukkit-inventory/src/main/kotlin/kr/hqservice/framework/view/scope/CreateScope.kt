package kr.hqservice.framework.view.scope

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.element.TeardownOptionCoroutineContextElement
import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.element.ButtonElement
import kr.hqservice.framework.view.state.SubscribableState

class CreateScope(private val view: View, private val coroutineScope: CoroutineScope) {
    internal val buttonJobs: MutableList<Job> = mutableListOf()

    fun button(vararg slots: Int, buttonScope: ButtonElement.() -> Unit) {
        slots.forEach { slot ->
            val button = ButtonElement(slot)
            view.registerButton(slot, button)
            buttonScope(button)
            val buttonJob = coroutineScope.launch {
                val buttonItemStack = button.itemStackBuilder.invoke(button.index)
                view.inventory.setItem(button.index, buttonItemStack)
            }
            buttonJobs.add(buttonJob)

            button.subscribedStates.map { state ->
                coroutineScope.launch(TeardownOptionCoroutineContextElement(true) + CoroutineName("HQFrameworkViewCreateScopeCoroutine")) {
                    buttonJob.join()
                    state as SubscribableState
                    state.getStateFlow().collect {
                        view.inventory.setItem(button.index, button.itemStackBuilder.invoke(button.index))
                    }
                }
            }.forEach { job ->
                view.subscribes.add(job)
            }
        }
    }

    fun button(intRange: IntRange, buttonScope: ButtonElement.() -> Unit) {
        this.button(slots = intRange.toList().toTypedArray().toIntArray(), buttonScope)
    }
}