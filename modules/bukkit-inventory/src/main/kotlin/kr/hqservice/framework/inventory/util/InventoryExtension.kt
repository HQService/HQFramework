package kr.hqservice.framework.inventory.util

import org.bukkit.inventory.Inventory

fun Inventory.hasSpace(): Boolean {
    return this.storageContents.any { it.type.isAir }
}