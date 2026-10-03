package kr.hqservice.framework.database.repository.player.lifecycle

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.Material
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoadGuardListenerTest {
    private lateinit var server: ServerMock
    private val loading = LoadingPlayers()
    private val listener = LoadGuardListener(loading)

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `hand swap and held slot changes are cancelled while loading`() {
        val player = server.addPlayer()
        loading.begin(player.uniqueId)
        val swap = PlayerSwapHandItemsEvent(player, ItemStack(Material.STONE), ItemStack(Material.AIR))
        val held = PlayerItemHeldEvent(player, 0, 1)

        listener.onSwapHand(swap)
        listener.onItemHeld(held)

        assertTrue(swap.isCancelled)
        assertTrue(held.isCancelled)
    }

    @Test
    fun `events of loaded players are not cancelled`() {
        val player = server.addPlayer()
        val swap = PlayerSwapHandItemsEvent(player, ItemStack(Material.STONE), ItemStack(Material.AIR))

        listener.onSwapHand(swap)

        assertFalse(swap.isCancelled)
    }
}
