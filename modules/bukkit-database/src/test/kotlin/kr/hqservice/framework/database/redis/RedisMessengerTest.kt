package kr.hqservice.framework.database.redis

import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

class RedisMessengerTest {
    @Serializable
    data class Party(val id: String, val members: List<String>)

    private val transport = InMemoryPubSubTransport()
    private val logger = mockk<Logger>(relaxed = true)
    private val messenger = RedisMessenger(transport, RedisSettings("", "hq", Duration.ofSeconds(3600)), Json, logger)

    @Test
    fun `published value reaches subscriber`() {
        val received = mutableListOf<Party>()
        messenger.subscribe("party", Party.serializer()) { received += it }

        messenger.publish("party", Party.serializer(), Party("p1", listOf("a", "b")))

        assertEquals(listOf(Party("p1", listOf("a", "b"))), received)
    }

    @Test
    fun `channel is prefixed`() {
        messenger.subscribe("party", Party.serializer()) {}
        assertEquals(setOf("hq:msg:party"), transport.listeners.keys)
    }

    @Test
    fun `malformed payload is skipped and logged`() {
        val received = mutableListOf<Party>()
        messenger.subscribe("party", Party.serializer()) { received += it }

        transport.publish("hq:msg:party", "not json".toByteArray())
        messenger.publish("party", Party.serializer(), Party("p2", emptyList()))

        assertEquals(listOf(Party("p2", emptyList())), received)
        verify { logger.log(Level.WARNING, any<String>(), any<Throwable>()) }
    }

    @Test
    fun `closed subscription stops delivery`() {
        val received = mutableListOf<Party>()
        val subscription = messenger.subscribe("party", Party.serializer()) { received += it }

        subscription.close()
        messenger.publish("party", Party.serializer(), Party("p3", emptyList()))

        assertTrue(received.isEmpty())
    }
}
