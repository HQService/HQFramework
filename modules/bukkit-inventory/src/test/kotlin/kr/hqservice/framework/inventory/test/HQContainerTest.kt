package kr.hqservice.framework.inventory.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kr.hqservice.framework.inventory.button.HQButtonBuilder
import kr.hqservice.framework.inventory.test.container.OtherContainer
import kr.hqservice.framework.inventory.test.container.Pager
import kr.hqservice.framework.inventory.test.container.TestContainer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class HQContainerTest {

    private lateinit var server: ServerMock
    private lateinit var player: Player

    @BeforeEach
    fun a() {
        server = MockBukkit.mock()
        MockBukkit.createMockPlugin("HQFramework")
        player = server.addPlayerWithCraftingView("ABC")
    }

    @Test
    fun containerTest() {
        TestContainer("&a테스트") {
            HQButtonBuilder(Material.BOOK)
                .setDisplayName("&a버튼1")
                .setClickFunction {

                }
                .build().setSlot(this, 0)
        }.open(player)

        val holder = player.openInventory.topInventory.holder
        assertTrue(holder is TestContainer)

        val button = (holder as TestContainer).getButton(0)
        assertEquals(button?.getDisplayName(), "§a버튼1")

        assertThrows<IndexOutOfBoundsException> {
            TestContainer("&a테스트2") {
                HQButtonBuilder(Material.BOOK).build().setSlot(this, 11)
            }.open(player)
        }
    }

    @Test
    fun openingContainerOfDifferentClassReplacesCurrentAfterOneTick() {
        val first = TestContainer("&afirst") {}
        val second = OtherContainer()

        first.open(player)
        second.open(player)
        server.scheduler.performOneTick()

        assertSame(second, player.openInventory.topInventory.holder)
    }

    @Test
    fun openingAnotherInstanceOfSameClassReplacesCurrentAfterOneTick() {
        val firstPage = Pager(1)
        val secondPage = Pager(2)

        firstPage.open(player)
        secondPage.open(player)
        server.scheduler.performOneTick()

        assertSame(secondPage, player.openInventory.topInventory.holder)
    }

    @Test
    fun replacingButtonOfSameTypeUpdatesAmount() {
        val container = TestContainer("&a") {
            HQButtonBuilder(Material.BOOK).build().setSlot(this, 0)
        }
        container.open(player)

        HQButtonBuilder(Material.BOOK, 5).setDisplayName("&bnew").build().setSlot(container, 0)

        assertEquals(5, container.inventory.getItem(0)?.amount)
    }

    @AfterEach
    fun b() {
        MockBukkit.unmock()
    }

}
