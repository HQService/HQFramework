package kr.hqservice.framework.region

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.location.impl.LazyBlockLocation
import org.bukkit.World
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BlockLocationEqualityTest {
    private lateinit var server: ServerMock
    private lateinit var world: World
    private lateinit var otherWorld: World

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        world = server.addSimpleWorld("w")
        otherWorld = server.addSimpleWorld("w2")
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

    @Test
    fun `block locations with colliding hashes are not equal`() {
        assertNotEquals(BlockLocationImpl(world, 0, 1, 0), BlockLocationImpl(world, 0, 0, 31))
        assertNotEquals(LazyBlockLocation("w", 0, 1, 0), LazyBlockLocation("w", 0, 0, 31))
    }

    @Test
    fun `block locations with same world and coordinates are equal`() {
        val a = BlockLocationImpl(world, 3, 64, -7)
        val b = BlockLocationImpl(world, 3, 64, -7)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())

        val lazyA = LazyBlockLocation("w", 3, 64, -7)
        val lazyB = LazyBlockLocation("w", 3, 64, -7)
        assertEquals(lazyA, lazyB)
        assertEquals(lazyA.hashCode(), lazyB.hashCode())

        assertEquals<Any>(a, lazyA)
        assertEquals<Any>(lazyA, a)
        assertEquals(a.hashCode(), lazyA.hashCode())
    }

    @Test
    fun `block locations in different worlds are not equal`() {
        assertNotEquals(BlockLocationImpl(world, 1, 2, 3), BlockLocationImpl(otherWorld, 1, 2, 3))
        assertNotEquals(LazyBlockLocation("w", 1, 2, 3), LazyBlockLocation("w2", 1, 2, 3))
        assertNotEquals<Any>(BlockLocationImpl(world, 1, 2, 3), LazyBlockLocation("w2", 1, 2, 3))
    }
}
