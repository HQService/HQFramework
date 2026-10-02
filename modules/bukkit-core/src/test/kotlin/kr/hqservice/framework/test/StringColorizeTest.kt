package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.bukkit.core.extension.gradient
import kr.hqservice.framework.global.core.extension.print
import net.md_5.bungee.api.ChatColor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.awt.Color
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class StringColorizeTest {
    @BeforeEach
    fun setup() {
        MockBukkit.mock()
    }

    @Test
    fun colorize_test() {
        val testString = "<g:ffffff>aaaaa</g:ff00ab>"
        testString.print("original -> ")
        testString.colorize().print("convert -> ")
    }

    @Test
    fun single_character_gradient_uses_start_color() {
        val result = "a".gradient("ff0000", "0000ff")
        assertEquals(ChatColor.of(Color(0xff0000)).toString() + "a", result)
    }

    @Test
    fun three_character_gradient_reaches_end_color() {
        val result = "abc".gradient("000000", "ffffff")
        assertTrue(result.startsWith(ChatColor.of(Color(0x000000)).toString() + "a"), result)
        assertTrue(result.endsWith(ChatColor.of(Color(0xffffff)).toString() + "c"), result)
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }
}