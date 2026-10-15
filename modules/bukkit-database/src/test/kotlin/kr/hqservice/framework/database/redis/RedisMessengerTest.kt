package kr.hqservice.framework.database.redis

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

@OptIn(ExperimentalCoroutinesApi::class)
class RedisMessengerTest {
    @Serializable
    data class Party(val id: String, val members: List<String>)

    private val transport = InMemoryPubSubTransport()
    private val logger = mockk<Logger>(relaxed = true)
    private val messenger = RedisMessenger(transport, RedisSettings("", "hq", Duration.ofSeconds(3600)), Json, logger)
    private val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher() + CoroutineName("plugin"))

    @Test
    fun `published value reaches subscriber`() {
        val received = mutableListOf<Party>()
        messenger.subscribe("party", Party.serializer(), scope) { received += it }

        messenger.publish("party", Party.serializer(), Party("p1", listOf("a", "b")))

        assertEquals(listOf(Party("p1", listOf("a", "b"))), received)
    }

    @Test
    fun `channel is prefixed`() {
        messenger.subscribe("party", Party.serializer(), scope) {}
        assertEquals(setOf("hq:msg:party"), transport.listeners.keys)
    }

    @Test
    fun `malformed payload is skipped and logged`() {
        val received = mutableListOf<Party>()
        messenger.subscribe("party", Party.serializer(), scope) { received += it }

        transport.publish("hq:msg:party", "not json".toByteArray())
        messenger.publish("party", Party.serializer(), Party("p2", emptyList()))

        assertEquals(listOf(Party("p2", emptyList())), received)
        verify { logger.log(Level.WARNING, any<String>(), any<Throwable>()) }
    }

    @Test
    fun `closed subscription stops delivery`() {
        val received = mutableListOf<Party>()
        val subscription = messenger.subscribe("party", Party.serializer(), scope) { received += it }

        subscription.close()
        subscription.close()
        messenger.publish("party", Party.serializer(), Party("p3", emptyList()))

        assertTrue(received.isEmpty())
        assertEquals(0, transport.listenerCount("hq:msg:party"))
    }

    @Test
    fun `handler runs inside the given scope`() {
        val names = mutableListOf<String?>()
        messenger.subscribe("party", Party.serializer(), scope) { names += currentCoroutineContext()[CoroutineName]?.name }

        messenger.publish("party", Party.serializer(), Party("p4", emptyList()))

        assertEquals(listOf("plugin"), names)
    }

    @Test
    fun `cancelling the scope removes the listener`() {
        val pluginScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
        messenger.subscribe("party", Party.serializer(), pluginScope) {}
        messenger.subscribe("party", Party.serializer(), scope) {}
        assertEquals(2, transport.listenerCount("hq:msg:party"))

        pluginScope.cancel()

        assertEquals(1, transport.listenerCount("hq:msg:party"))
    }

    @Test
    fun `a failing handler does not affect other subscriptions`() {
        val failures = mutableListOf<Throwable>()
        val guarded = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher() + CoroutineExceptionHandler { _, e -> failures += e })
        val received = mutableListOf<String>()
        messenger.subscribe("party", Party.serializer(), guarded) { throw IllegalStateException("broken ${it.id}") }
        messenger.subscribe("party", Party.serializer(), guarded) { received += it.id }

        messenger.publish("party", Party.serializer(), Party("p5", emptyList()))
        messenger.publish("party", Party.serializer(), Party("p6", emptyList()))

        assertEquals(listOf("p5", "p6"), received)
        assertEquals(listOf("broken p5", "broken p6"), failures.map { it.message })
    }
}
