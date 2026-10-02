package kr.hqservice.framework.nms.legacy.virtual.container

import org.bukkit.event.inventory.InventoryType
import kotlin.reflect.KClass

enum class VirtualContainerType(
    vararg val inventoryTypeNames: String
) {
    GENERIC_9X1("CHEST"),
    GENERIC_9X2("CHEST"),
    GENERIC_9X3("CHEST", "ENDER_CHEST", "BARREL"),
    GENERIC_9X4("CHEST"),
    GENERIC_9X5("CHEST"),
    GENERIC_9X6("CHEST"),
    GENERIC_3X3("DISPENSER", "DROPPER"),
    ANVIL("ANVIL"),
    BEACON("BEACON"),
    BREWING_STAND("BREWING"),
    ENCHANTMENT("ENCHANTING"),
    FURNACE("FURNACE"),
    HOPPER("HOPPER"),
    MERCHANT("MERCHANT"),
    SHULKER_BOX("SHULKER_BOX"),
    BLAST_FURNACE("BLAST_FURNACE"),
    CRAFTING("WORKBENCH"),
    GRINDSTONE("GRINDSTONE"),
    LECTERN("LECTERN"),
    LOOM("LOOM"),
    SMOKER("SMOKER"),
    CARTOGRAPHY_TABLE("CARTOGRAPHY"),
    STONECUTTER("STONECUTTER"),
    SMITHING("SMITHING");

    companion object {
        private val alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        private val alphabet_v2 = "abcdefgijklmnopqrstuvwxyz".toCharArray()

        fun getType(type: InventoryType, size: Int): VirtualContainerType? {
            if (type == InventoryType.CHEST) return VirtualContainerType.valueOf("GENERIC_9X${size / 9}")
            return values().firstOrNull {
                it.inventoryTypeNames.contains(type.name)
            }
        }
    }

    fun getVirtualType(containersClass: KClass<*>, v2: Boolean = false): Any {
        return containersClass.java.getField((if (v2) alphabet_v2 else alphabet)[ordinal].toString()).get(null)
    }
}