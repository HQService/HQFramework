package kr.hqservice.framework.database.repository.player.lock

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class SwitchGateTest {
    @Test
    fun `signal before ensure is remembered`() {
        val gate = SwitchGate()
        val id = UUID.randomUUID()
        gate.signal(id)
        assertTrue(gate.ensure(id).isCompleted)
    }

    @Test
    fun `ensure then signal completes the waiting gate`() {
        val gate = SwitchGate()
        val id = UUID.randomUUID()
        val waiting = gate.ensure(id)
        gate.signal(id)
        assertTrue(waiting.isCompleted)
    }

    @Test
    fun `release then ensure returns a fresh gate`() {
        val gate = SwitchGate()
        val id = UUID.randomUUID()
        gate.ensure(id)
        gate.signal(id)
        gate.release(id)
        assertFalse(gate.ensure(id).isCompleted)
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
