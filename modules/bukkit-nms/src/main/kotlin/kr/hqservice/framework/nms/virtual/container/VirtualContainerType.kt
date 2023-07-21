package kr.hqservice.framework.nms.virtual.container

import org.bukkit.event.inventory.InventoryType
import kotlin.reflect.KClass

enum class VirtualContainerType(
    val containerVersion: Int,
    val minecraftName: String?,
    vararg val inventoryTypeNames: String
) {
    GENERIC_9X1(14, "minecraft:chest", "CHEST"),
    GENERIC_9X2(14, "minecraft:chest", "CHEST"),
    GENERIC_9X3(14, "minecraft:chest", "CHEST", "ENDER_CHEST", "BARREL"),
    GENERIC_9X4(14, "minecraft:chest", "CHEST"),
    GENERIC_9X5(14, "minecraft:chest", "CHEST"),
    GENERIC_9X6(14, "minecraft:chest", "CHEST"),
    GENERIC_3X3(14, null, "DISPENSER", "DROPPER"),
    ANVIL(14, "minecraft:anvil", "ANVIL"),
    BEACON(14, "minecraft:beacon", "BEACON"),
    BREWING_STAND(14, "minecraft:brewing_stand", "BREWING"),
    ENCHANTMENT(14, "minecraft:enchanting_table", "ENCHANTING"),
    FURNACE(14, "minecraft:furnace", "FURNACE"),
    HOPPER(14, "minecraft:hopper", "HOPPER"),
    MERCHANT(14, "minecraft:villager", "MERCHANT"),
    SHULKER_BOX(14, "minecraft:blue_shulker_box", "SHULKER_BOX"),
    BLAST_FURNACE(14, null, "BLAST_FURNACE"),
    CRAFTING(14, null, "WORKBENCH"),
    GRINDSTONE(14, null, "GRINDSTONE"),
    LECTERN(14, null, "LECTERN"),
    LOOM(14, null, "LOOM"),
    SMOKER(14, null, "SMOKER"),
    CARTOGRAPHY_TABLE(14, null, "CARTOGRAPHY"),
    STONECUTTER(14, null, "STONECUTTER"),
    SMITHING(16, null, "SMITHING");

    companion object {
        private val alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray()

        fun getType(type: InventoryType, size: Int): VirtualContainerType? {
            if (type == InventoryType.CHEST) return VirtualContainerType.valueOf("GENERIC_9X${size / 9}")
            return values().firstOrNull {
                it.inventoryTypeNames.contains(type.toString())
            }
        }
    }

    fun getVirtualType(containersClass: KClass<*>): Any {
        return containersClass.java.getField(alphabet[ordinal].toString()).get(null)
    }
}