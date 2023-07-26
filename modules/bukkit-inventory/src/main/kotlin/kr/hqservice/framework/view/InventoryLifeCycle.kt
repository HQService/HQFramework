package kr.hqservice.framework.view

import kr.hqservice.framework.view.coroutine.LifecycleOwner
import kr.hqservice.framework.view.element.ButtonElement
import org.bukkit.inventory.InventoryHolder

interface InventoryLifecycle : InventoryHolder, LifecycleOwner {
    fun registerButton(slot: Int, buttonElement: ButtonElement)
}