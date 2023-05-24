package kr.hqservice.framework.test

import be.seeseemelk.mockbukkit.MockBukkit
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.region.extension.asBlockLocation
import org.bukkit.Location
import org.bukkit.World
import kr.hqservice.framework.region.extension.rangeTo
import kr.hqservice.framework.region.range.DimensionRange
import kr.hqservice.framework.region.range.LineRange
import kr.hqservice.framework.region.range.enums.LineAxis
import kr.hqservice.framework.region.range.enums.Offset
import kr.hqservice.framework.region.range.enums.PlaneAxis
import org.bukkit.Material
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RangeTest {
    private lateinit var plugin: HQPlugin
    private lateinit var world: World

    @BeforeEach
    fun setup() {
        val server = MockBukkit.mock()
        plugin = HQFrameworkMock.mock("RangeTest")
        world = server.addSimpleWorld("world")
    }

    @Test
    fun createRangeTest() {
        val blockLocation1 = Location(world, -50.5, 1.5, 10.5).asBlockLocation()
        val blockLocation2 = Location(world, 12.5, 55.5, 125.5).asBlockLocation()
        val range = blockLocation1 .. blockLocation2

        assertIs<DimensionRange>(range)
        assertIsNot<LineRange>(range)

        val planeRange = range.getPlaneRange(PlaneAxis.VERTICAL_X, Offset.CENTER)
        assertEquals(planeRange.minPosition.getX(), planeRange.maxPosition.getX())
        val lineRange = planeRange.getLineRange(LineAxis.HORIZONTAL_X, Offset.MAX)
        assertEquals(lineRange.minPosition.getY(), lineRange.maxPosition.getY())
        val point = lineRange.getPoint(Offset.CENTER)
        assertEquals(point.minPosition, point.maxPosition)
        collisionTest()
    }

    @Test
    fun collisionTest() {
        val blockLocation1 = Location(world, -50.5, 1.5, 10.5).asBlockLocation()
        val blockLocation2 = Location(world, 12.5, 55.5, 125.5).asBlockLocation()
        val range = blockLocation1 .. blockLocation2

        val blockLocation3 = Location(world, 10.5, 10.2, 22.5).asBlockLocation()
        val blockLocation4 = Location(world, -20.5, 22.2, 50.5).asBlockLocation()
        assertTrue(range.contains(blockLocation3))

        val otherRange = blockLocation3 .. blockLocation4

        assertTrue(range.collidesWith(otherRange))
        assertTrue(otherRange.collidesWith(range))
    }

    @Test
    fun iterableTest() {
        val blockLocation1 = Location(world, 5.5, 55.5, 10.5).asBlockLocation()
        val blockLocation2 = Location(world, 5.5, 55.5, 20.5).asBlockLocation()
        val range = blockLocation1 .. blockLocation2
        range.print("range_instance= ")
        range.minPosition.print("min= ")
        range.maxPosition.print("max= ")


        val iter = range.iterator()
        while(iter.hasNext()) {
            val next = iter.next()
            next.print("next= ")
        }

        range.forEach {
            it.getBlock().type = Material.STONE }

        assertEquals(range.random().getBlock().type, Material.STONE)
    }

    @AfterEach
    fun teardown() {
        HQFrameworkMock.unmock()
        MockBukkit.unmock()
    }
}