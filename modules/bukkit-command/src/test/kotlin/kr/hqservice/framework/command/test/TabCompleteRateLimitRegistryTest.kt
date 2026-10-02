package kr.hqservice.framework.command.test

import io.mockk.every
import io.mockk.mockk
import kr.hqservice.framework.command.registry.impl.TabCompleteRateLimitRegistryImpl
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TabCompleteRateLimitRegistryTest {
    private var now = 10_000L

    private fun registry(limit: Int?): TabCompleteRateLimitRegistryImpl {
        val config = mockk<HQYamlConfiguration>()
        every { config.getInt(any(), any()) } answers { limit ?: secondArg() }
        return TabCompleteRateLimitRegistryImpl(config) { now }
    }

    @Test
    fun `allows exactly the limit within one second and again after it`() {
        val registry = registry(3)
        val id = UUID.randomUUID()
        repeat(3) { assertTrue(registry.isTabCompletable(id)) }
        assertFalse(registry.isTabCompletable(id))
        now += 1001
        assertTrue(registry.isTabCompletable(id))
    }

    @Test
    fun `missing limit defaults to twenty per second`() {
        val registry = registry(null)
        val id = UUID.randomUUID()
        assertEquals(20, (1..25).count { registry.isTabCompletable(id) })
    }
}
