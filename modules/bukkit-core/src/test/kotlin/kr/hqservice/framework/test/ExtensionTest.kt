package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kr.hqservice.framework.bukkit.core.extension.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Collections

class ExtensionTest {

    private lateinit var mock: ServerMock
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        mock = MockBukkit.mock()
        player = mock.addPlayer("Cosine_A")
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun time_test() {
        println("2022년 06월 28일 15시 01분 13초".toLocalDateTime())
    }

    @Test
    fun player_test() {

    }

    @Test
    fun number_test() {
        println("10".isInt())
    }

    @Test
    fun inventory_test() {
        val inventory = player.inventory
        repeat(36) {
            inventory.setItem(it, ItemStack(Material.STONE))
        }
        println(inventory.getEmptySlots())
        println(inventory.isAddable(ItemStack(Material.BOOK)))
    }

    @Test
    fun percent_test() {
        val map = mapOf("가" to 1.0, "나" to 10.0, "다" to 50.0, "라" to 70.0)
        val result = mutableListOf<String>()
        repeat(100) {
            result.add(map.random())
        }
        println("가: " + result.getCount("가"))
        println("나: " + result.getCount("나"))
        println("다: " + result.getCount("다"))
        println("라: " + result.getCount("라"))
    }

    private fun List<String>.getCount(value: String) = Collections.frequency(this, value)

    @Test
    fun count_test() {
        val list = listOf("가", "가", "가", "나", "다", "다")
        println(list.getCount("다"))
    }
}