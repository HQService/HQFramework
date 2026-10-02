package kr.hqservice.framework.database.column

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import org.bukkit.Location
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LocationColumnTest {
    private lateinit var server: ServerMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    @Test
    fun `world name containing a separator survives a round trip`() {
        val world = server.addSimpleWorld("a;b")
        val location = Location(world, 1.5, 64.0, -3.25, 90f, 45f)

        val serialized = serializeLocation(location)
        val parsed = parseLocation(serialized)

        assertEquals("1.5;64.0;-3.25;90.0;45.0;a;b", serialized)
        assertEquals(location, parsed)
    }

    @Test
    fun `legacy world first format is still parsed`() {
        val world = server.addSimpleWorld("legacy")

        val parsed = parseLocation("legacy;1.5;64.0;-3.25;90.0;45.0")

        assertEquals(Location(world, 1.5, 64.0, -3.25, 90f, 45f), parsed)
    }
}
