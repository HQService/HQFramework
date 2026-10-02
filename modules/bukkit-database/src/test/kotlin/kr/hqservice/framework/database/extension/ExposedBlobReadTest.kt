package kr.hqservice.framework.database.extension

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kr.hqservice.framework.bukkit.core.extension.toItemArray
import kr.hqservice.framework.bukkit.core.extension.toItemStack
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExposedBlobReadTest {
    private val itemStackExtensions = "kr.hqservice.framework.bukkit.core.extension.ItemStackExtensionKt"
    private val decodedPayloads = mutableListOf<List<Byte>>()

    @BeforeEach
    fun setUp() {
        mockkStatic(itemStackExtensions)
        every { any<ByteArray>().toItemStack(any()) } answers {
            decodedPayloads.add(firstArg<ByteArray>().toList())
            mockk(relaxed = true)
        }
        every { any<ByteArray>().toItemArray(any()) } answers {
            decodedPayloads.add(firstArg<ByteArray>().toList())
            emptyArray<ItemStack>()
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(itemStackExtensions)
    }

    @Test
    fun `item stack blob can be read more than once`() {
        val blob = ExposedBlob(byteArrayOf(1, 2, 3))

        blob.toItemStack()
        blob.toItemStack()

        assertEquals(listOf(listOf<Byte>(1, 2, 3), listOf<Byte>(1, 2, 3)), decodedPayloads)
    }

    @Test
    fun `item array blob can be read more than once`() {
        val blob = ExposedBlob(byteArrayOf(4, 5))

        blob.toItemArray()
        blob.toItemArray()

        assertEquals(listOf(listOf<Byte>(4, 5), listOf<Byte>(4, 5)), decodedPayloads)
    }
}
