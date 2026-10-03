package kr.hqservice.framework.database.redis.handler

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kr.hqservice.framework.database.TestPlugin
import kr.hqservice.framework.database.redis.InMemoryPubSubTransport
import kr.hqservice.framework.database.redis.RedisChannel
import kr.hqservice.framework.database.redis.RedisMessenger
import kr.hqservice.framework.database.redis.RedisSettings
import kr.hqservice.framework.database.redis.RedisSubscriber
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.Collections
import java.util.logging.Logger
import kotlin.reflect.full.findAnnotation

class RedisSubscriberAnnotationHandlerTest {
    @Serializable
    data class PartyInvite(val partyId: String, val target: String)

    @RedisSubscriber
    class InviteSubscriber {
        val received: MutableList<PartyInvite> = Collections.synchronizedList(mutableListOf())
        val names: MutableList<List<String>> = Collections.synchronizedList(mutableListOf())

        @RedisChannel("party-invite")
        suspend fun onInvite(invite: PartyInvite) {
            received += invite
        }

        @RedisChannel("party-names")
        fun onNames(names: List<String>) {
            this.names += names
        }
    }

    @RedisSubscriber
    class TwoParameters {
        @RedisChannel("broken")
        fun onMessage(invite: PartyInvite, extra: String) {}
    }

    private lateinit var server: ServerMock
    private lateinit var plugin: TestPlugin
    private val transport = InMemoryPubSubTransport()
    private val logger = mockk<Logger>(relaxed = true)
    private val settings = RedisSettings("redis://localhost:6379/0", "hq", Duration.ofSeconds(3600))
    private val messenger = RedisMessenger(transport, settings, Json, logger)

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = TestPlugin.load(server)
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }

    private fun handler(settings: RedisSettings = this.settings) = RedisSubscriberAnnotationHandler(messenger, settings, Json, plugin, logger)

    private fun annotation(instance: Any): RedisSubscriber = instance::class.findAnnotation()!!

    private fun awaitUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 3000
        while (System.currentTimeMillis() < deadline) {
            server.scheduler.performTicks(1)
            if (condition()) return
            Thread.sleep(10)
        }
        fail<Unit>("condition not met")
    }

    @Test
    fun `annotated functions receive messages decoded from the parameter type`() {
        val subscriber = InviteSubscriber()
        handler().setup(subscriber, annotation(subscriber))

        messenger.publish("party-invite", PartyInvite.serializer(), PartyInvite("p1", "steve"))
        messenger.publish("party-names", ListSerializer(String.serializer()), listOf("a", "b"))

        awaitUntil { subscriber.received.isNotEmpty() && subscriber.names.isNotEmpty() }
        assertEquals(listOf(PartyInvite("p1", "steve")), subscriber.received)
        assertEquals(listOf(listOf("a", "b")), subscriber.names)
    }

    @Test
    fun `teardown closes only the subscriptions of that instance`() {
        val first = InviteSubscriber()
        val second = InviteSubscriber()
        val handler = handler()
        handler.setup(first, annotation(first))
        handler.setup(second, annotation(second))
        assertEquals(2, transport.listenerCount("hq:msg:party-invite"))

        handler.teardown(first, annotation(first))

        assertEquals(1, transport.listenerCount("hq:msg:party-invite"))
        assertEquals(1, transport.listenerCount("hq:msg:party-names"))
        messenger.publish("party-invite", PartyInvite.serializer(), PartyInvite("p2", "alex"))
        awaitUntil { second.received.isNotEmpty() }
        assertEquals(emptyList<PartyInvite>(), first.received)
    }

    @Test
    fun `a channel function must take exactly one parameter`() {
        val subscriber = TwoParameters()

        val exception = assertThrows(IllegalStateException::class.java) { handler().setup(subscriber, annotation(subscriber)) }

        assertEquals(true, exception.message!!.contains("onMessage"))
        assertEquals(0, transport.listenerCount("hq:msg:broken"))
    }

    @Test
    fun `without redis uri nothing is subscribed and a warning is logged`() {
        val subscriber = InviteSubscriber()

        handler(RedisSettings("", "hq", Duration.ofSeconds(3600))).setup(subscriber, annotation(subscriber))

        assertEquals(0, transport.listenerCount("hq:msg:party-invite"))
        verify { logger.warning(match<String> { it.contains("InviteSubscriber") && it.contains("redis.uri") }) }
    }
}
