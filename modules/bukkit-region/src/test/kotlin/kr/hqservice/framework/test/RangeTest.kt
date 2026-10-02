package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.region.extension.asBlockLocation
import kr.hqservice.framework.region.extension.rangeTo
import kr.hqservice.framework.region.location.BlockLocation
import kr.hqservice.framework.region.location.impl.BlockLocationImpl
import kr.hqservice.framework.region.math.Point
import kr.hqservice.framework.region.range.DimensionRange
import kr.hqservice.framework.region.range.LineRange
import kr.hqservice.framework.region.range.PlaneRange
import kr.hqservice.framework.region.range.PointRange
import kr.hqservice.framework.region.range.enums.LineAxis
import kr.hqservice.framework.region.range.enums.Offset
import kr.hqservice.framework.region.range.enums.PlaneAxis
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.stopKoin
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertIsNot
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RangeTest {
    private lateinit var plugin: HQBukkitPlugin
    private lateinit var world: World

    @BeforeEach
    fun setup() {
        val server = MockBukkit.mock()
        plugin = HQFrameworkBukkitMock.mock("RangeTest")
        world = server.addSimpleWorld("world")
    }

    @Test
    fun createRangeTest() {
        val blockLocation1 = Location(world, -50.5, 1.5, 10.5).asBlockLocation()
        val blockLocation2 = Location(world, 12.5, 55.5, 125.5).asBlockLocation()
        val range = blockLocation1..blockLocation2

        assertIs<DimensionRange>(range)
        assertIsNot<LineRange>(range)

        val planeRange = range.getPlaneRange(PlaneAxis.VERTICAL_X, Offset.CENTER)
        assertEquals(planeRange.minPosition.getX(), planeRange.maxPosition.getX())
        val lineRange = planeRange.getLineRange(LineAxis.HORIZONTAL_X, Offset.MAX)
        assertEquals(lineRange.minPosition.getY(), lineRange.maxPosition.getY())
        val point = lineRange.getPoint(Offset.CENTER)
        assertEquals(point.minPosition, point.maxPosition)

        assertIs<PointRange>(at(1, 2, 3)..at(1, 2, 3))
        assertIs<LineRange>(at(1, 2, 3)..at(1, 2, 9))
        assertIs<PlaneRange>(at(1, 2, 3)..at(1, 8, 9))
        assertIs<DimensionRange>(at(1, 2, 3)..at(7, 8, 9))
        assertIs<PlaneRange>(at(0, 1, 0)..at(0, 0, 31))
    }

    private fun at(x: Int, y: Int, z: Int): BlockLocation = BlockLocationImpl(world, x, y, z)

    @Test
    fun linePointKeepsFixedAxes() {
        val alongZ = (at(5, 55, 10)..at(5, 55, 20)) as LineRange
        assertEquals(Point(5, 55, 15), alongZ.getPoint(Offset.CENTER).minPosition.getPoint())

        val alongX = (at(0, 64, 3)..at(10, 64, 3)) as LineRange
        assertEquals(Point(5, 64, 3), alongX.getPoint(Offset.CENTER).minPosition.getPoint())
        assertEquals(Point(7, 64, 3), alongX.getPoint(2).minPosition.getPoint())

        val vertical = (at(3, 10, 7)..at(3, 20, 7)) as LineRange
        assertEquals(Point(3, 15, 7), vertical.getPoint(Offset.CENTER).minPosition.getPoint())
        assertEquals(Point(3, 20, 7), vertical.getPoint(Offset.MAX).minPosition.getPoint())
    }

    @Test
    fun largeRangeIsLazy() {
        assertTimeoutPreemptively(Duration.ofSeconds(2)) {
            val range = at(0, 0, 0)..at(199, 383, 199)
            assertEquals(200 * 384 * 200, range.size)
            assertEquals(Point(0, 0, 0), range.first().getPoint())
            assertTrue(range.contains(at(100, 100, 100)))
            assertFalse(range.contains(at(200, 100, 100)))
        }
    }

    @Test
    fun collidesWithIsInclusiveAabb() {
        val range = at(0, 0, 0)..at(10, 10, 10)
        assertTrue(range.collidesWith(at(5, 5, 5)..at(20, 20, 20)))
        assertFalse(range.collidesWith(at(11, 0, 0)..at(20, 10, 10)))
        assertTrue(range.collidesWith(at(10, 0, 0)..at(20, 10, 10)))
        assertTrue((at(10, 0, 0)..at(20, 10, 10)).collidesWith(range))
    }

    @Test
    fun centerFloorsNegativeCoordinates() {
        val range = at(-5, 0, 0)..at(-2, 0, 0)
        assertEquals(-4, range.getCenter().getX())
    }

    @Test
    fun collisionTest() {
        val blockLocation1 = Location(world, -50.5, 1.5, 10.5).asBlockLocation()
        val blockLocation2 = Location(world, 12.5, 55.5, 125.5).asBlockLocation()
        val range = blockLocation1..blockLocation2

        val blockLocation3 = Location(world, 10.5, 10.2, 22.5).asBlockLocation()
        val blockLocation4 = Location(world, -20.5, 22.2, 50.5).asBlockLocation()
        assertTrue(range.contains(blockLocation3))

        val otherRange = blockLocation3..blockLocation4

        assertTrue(range.collidesWith(otherRange))
        assertTrue(otherRange.collidesWith(range))
    }

    @Test
    fun iterableTest() {
        val blockLocation1 = Location(world, 5.5, 55.5, 10.5).asBlockLocation()
        val blockLocation2 = Location(world, 5.5, 55.5, 20.5).asBlockLocation()
        val range = blockLocation1..blockLocation2
        var count = 0
        val iter = range.iterator()
        while (iter.hasNext()) {
            iter.next()
            count++
        }
        assertEquals(range.size, count)
        assertEquals(11, count)

        range.forEach {
            it.getBlock().type = Material.STONE
        }

        assertEquals(range.random().getBlock().type, Material.STONE)
    }

    @AfterEach
    fun teardown() {
        HQFrameworkBukkitMock.unmock()
        MockBukkit.unmock()
        stopKoin()
    }
}