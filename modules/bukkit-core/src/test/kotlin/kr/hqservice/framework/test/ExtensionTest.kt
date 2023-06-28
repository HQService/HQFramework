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
        val time = LocalDateTime.now().plusHours(-2)
        println("시간 지남: " + time.isTimeAfter())
    }

    @Test
    fun player_test() {
        println("%player%님은 %age%살입니다.".replace(
            "%player%" to "Cosine_A", "%age%" to "20"
        ))
    }

    @Test
    fun list_test() {
        val list = listOf("%player%는 %age%살입니다.", "%age%살은 MZ입니다.").replace(
            "%player%" to "Cosine_A",
            "%age%" to "20"
        )
        list.forEach(::println)
    }

    @Test
    fun number_test() {
        println("Int인지: " + "10".isInt())
        println("Long인지: " + "10000000000000000000000".isLong())
        println("Double인지: " + "10.0".isDouble())
        println("Float인지: " + "1000.0".isFloat())
    }

    @Test
    fun inventory_test() {
        val inventory = player.inventory
        repeat(36) {
            inventory.setItem(it, ItemStack(Material.STONE))
        }
        //println("남은 슬롯: " + inventory.getEmptySlots())
        println("BOOK 추가 가능한지: " + inventory.isAddable(ItemStack(Material.BOOK)))
        println("STONE 추가 가능한지: " + inventory.isAddable(ItemStack(Material.STONE)))
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
        println("다 갯수: " + list.getCount("다"))
    }
}