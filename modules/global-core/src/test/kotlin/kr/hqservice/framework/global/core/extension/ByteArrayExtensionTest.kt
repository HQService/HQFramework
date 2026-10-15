package kr.hqservice.framework.global.core.extension

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ByteArrayExtensionTest {
    @Test
    fun `compressed bytes inflate back to the original`() {
        val original = "Hello world".repeat(1000).toByteArray()
        assertArrayEquals(original, original.compress().decompress())
    }

    @Test
    fun `inflating past the limit is rejected`() {
        val bomb = ByteArray(33 * 1024 * 1024).compress()
        assertThrows<IllegalArgumentException> { bomb.decompress() }
    }
}
