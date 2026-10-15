package kr.hqservice.framework.inventory.test

import be.seeseemelk.mockbukkit.MockBukkit
import kr.hqservice.framework.inventory.button.HQButtonBuilder
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HQButtonBuilderTest {

    @BeforeEach
    fun setUp() {
        MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun customModelDataIsAbsentWhenZero() {
        val itemStack = HQButtonBuilder(Material.BOOK).build().getItemStack()

        assertFalse(itemStack.itemMeta!!.hasCustomModelData())
    }

    @Test
    fun customModelDataIsAppliedWhenNonZero() {
        val itemStack = HQButtonBuilder(Material.BOOK).setCustomModelData(7).build().getItemStack()

        assertEquals(7, itemStack.itemMeta!!.customModelData)
    }

    @Test
    fun glowHidesEnchantments() {
        val itemStack = HQButtonBuilder(Material.BOOK).setGlow(true).build().getItemStack()

        assertTrue(itemStack.itemMeta!!.hasItemFlag(ItemFlag.HIDE_ENCHANTS))
    }
}
