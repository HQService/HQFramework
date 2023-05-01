package kr.hqservice.framework.inventory.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kr.hqservice.framework.inventory.button.HQButtonBuilder
import kr.hqservice.framework.inventory.test.container.TestContainer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class HQContainerTest {

    private lateinit var server: ServerMock
    private lateinit var player: Player

    @BeforeEach
    fun a() {
        server = MockBukkit.mock()
        player = server.addPlayer("ABC")
    }

    @Test
    fun containerTest() {
        TestContainer("&a테스트") {
            HQButtonBuilder(Material.BOOK) {
                displayName = "&a버튼1"
            }.build().setSlot(this, 0)
        }.open(player)

        val holder = player.openInventory.topInventory.holder
        assertTrue(holder is TestContainer)

        val button = (holder as TestContainer).getButton(0)
        assertEquals(button?.getDisplayName(), "§a버튼1")

        assertThrows<IndexOutOfBoundsException> {
            TestContainer("&a테스트2") {
                HQButtonBuilder(Material.BOOK) {
                    displayName = "&a버튼2"
                }.build().setSlot(this, 11)
            }.open(player)
        }
    }


    @AfterEach
    fun b() {
        MockBukkit.unmock()
    }

}