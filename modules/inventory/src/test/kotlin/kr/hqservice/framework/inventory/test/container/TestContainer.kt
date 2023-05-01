package kr.hqservice.framework.inventory.test.container

import kr.hqservice.framework.inventory.container.HQContainer
import org.bukkit.inventory.Inventory

class TestContainer(
    title: String,
    private val settingBlock: HQContainer.()-> Unit
): HQContainer(9, title) {

    override fun initializing(inventory: Inventory) {
        settingBlock.invoke(this)
    }

}