package kr.hqservice.framework.inventory.util

import org.bukkit.inventory.Inventory

fun Inventory.hasSpace(size: Int = 1): Boolean {
    return this.storageContents.filter { it?.type?.isAir != false }.size >= size
}