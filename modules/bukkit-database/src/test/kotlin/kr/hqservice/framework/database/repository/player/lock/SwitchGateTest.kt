package kr.hqservice.framework.database.repository.player.lock

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class SwitchGateTest {
    @Test
    fun `ensure after signal returns a fresh gate`() = runTest {
        val gate = SwitchGate()
        val id = UUID.randomUUID()
        gate.ensure(id)
        gate.signal(id)
        val second = gate.ensure(id)
        assertFalse(second.isCompleted)
    }

    @Test
    fun `signal completes the waiting gate`() = runTest {
        val gate = SwitchGate()
        val id = UUID.randomUUID()
        val waiting = gate.ensure(id)
        gate.signal(id)
        assertTrue(waiting.isCompleted)
    }

    @Test
    fun `release removes the entry`() {
        val gate = SwitchGate()
        val id = UUID.randomUUID()
        gate.ensure(id)
        gate.release(id)
        assertTrue(gate.isEmpty())
    }

    @Test
    fun `clear empties the map`() {
        val gate = SwitchGate()
        gate.ensure(UUID.randomUUID())
        gate.clear()
        assertTrue(gate.isEmpty())
    }
}
