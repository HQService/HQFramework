package kr.hqservice.framework.bukkit.scheduler

import io.mockk.every
import io.mockk.mockk
import kr.hqservice.framework.bukkit.scheduler.database.HQFrameworkJobStore
import kr.hqservice.framework.yaml.config.impl.HQYamlConfigurationImpl
import org.bukkit.Server
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class SchedulerConfigDefaultsTest {
    private val emptyConfig = HQYamlConfigurationImpl()

    private fun server(ip: String, port: Int): Server {
        val server = mockk<Server>()
        every { server.ip } returns ip
        every { server.port } returns port
        return server
    }

    @Test
    fun `job store defaults match the bundled config`() {
        val store = HQFrameworkJobStore(emptyConfig, server("", 25565), Logger.getAnonymousLogger(), mockk(relaxed = true))

        assertFalse(store.isClustered)
        assertTrue(store.canUseProperties())
        assertEquals(1100L, store.misfireThreshold)
        assertEquals(2000L, store.clusterCheckinInterval)
    }

    @Test
    fun `instance id prefers the configured server ip`() {
        assertEquals("10.0.0.5:25565", resolveSchedulerInstanceId(emptyConfig, server("10.0.0.5", 25565)))
    }

    @Test
    fun `instance id falls back to the host name when the server ip is blank`() {
        val instanceId = resolveSchedulerInstanceId(emptyConfig, server(" ", 25565))

        assertTrue(instanceId.endsWith(":25565"))
        assertFalse(instanceId.startsWith(":"))
    }
}
