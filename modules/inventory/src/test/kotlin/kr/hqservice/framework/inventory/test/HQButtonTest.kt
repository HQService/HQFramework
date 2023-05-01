package kr.hqservice.framework.inventory.test

import be.seeseemelk.mockbukkit.MockBukkit
import kr.hqservice.framework.inventory.button.HQButtonBuilder
import kr.hqservice.framework.inventory.exception.IllegalMaterialException
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class HQButtonTest {

    @BeforeEach
    fun a() {
        MockBukkit.mock()
    }

    @Test
    fun exceptionTest() {
        assertThrows<IllegalMaterialException> {
            HQButtonBuilder(Material.AIR)
            HQButtonBuilder(Material.POTATOES)
        }
        assertDoesNotThrow {
            HQButtonBuilder(Material.STONE)
            HQButtonBuilder(Material.POTATO)
        }
    }

    @Test
    fun builderTest() {
        val button = HQButtonBuilder(Material.STONE) {
            displayName = "&a하이"
            lore = listOf(
                "&a첫 줄",
                "&c두 번째 줄"
            )
            itemFlags = setOf(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_DESTROYS
            )
            glow = true
        }.build()

        val item = button.getItemStack()
        assertEquals(item.itemMeta?.displayName, "§a하이")
        assertLinesMatch(item.itemMeta?.lore, listOf("§a첫 줄", "§c두 번째 줄"))
        assertIterableEquals(item.itemMeta?.itemFlags, setOf(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS))
        assertEquals(item.getEnchantmentLevel(Enchantment.LURE), 1)
    }

    @AfterEach
    fun b() {
        MockBukkit.unmock()
    }

}