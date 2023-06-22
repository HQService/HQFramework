package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.global.core.extension.print
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

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

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }
}