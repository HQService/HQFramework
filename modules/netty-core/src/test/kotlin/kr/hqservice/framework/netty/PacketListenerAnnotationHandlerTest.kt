package kr.hqservice.framework.netty

import io.mockk.mockk
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.listener.PacketListener
import kr.hqservice.framework.netty.packet.listener.PacketListenerAnnotationHandler
import kr.hqservice.framework.netty.packet.listener.PacketSubscribe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.reflect.full.findAnnotation

class GreetPacket(val name: String) : Packet() {
    override fun write(buf: ByteBuf) {}
    override fun read(buf: ByteBuf) {}
}

class ByePacket(val name: String) : Packet() {
    override fun write(buf: ByteBuf) {}
    override fun read(buf: ByteBuf) {}
}

@PacketListener(outbound = [ByePacket::class])
class GreetListener {
    val seen = mutableListOf<String>()

    @PacketSubscribe
    suspend fun onGreet(packet: GreetPacket, channel: ChannelWrapper) {
        delay(1)
        seen += "greet:${packet.name}"
    }

    @PacketSubscribe
    fun onBye(packet: ByePacket) {
        seen += "bye:${packet.name}"
    }

    fun helper(packet: GreetPacket) {
        seen += "never"
    }
}

@PacketListener
class BrokenListener {
    @PacketSubscribe
    fun noPacket(name: String) {}
}

class PacketListenerAnnotationHandlerTest {
    private val handler = PacketListenerAnnotationHandler(Logger.getLogger("test"))
    private val channel = mockk<ChannelWrapper>(relaxed = true)

    @AfterEach
    fun cleanUp() {
        Direction.INBOUND.unregisterPacket(GreetPacket::class)
        Direction.INBOUND.unregisterPacket(ByePacket::class)
        Direction.OUTBOUND.unregisterPacket(ByePacket::class)
    }

    @Test
    fun `annotated functions receive packets and outbound types are registered`() = runBlocking {
        val listener = GreetListener()
        handler.setup(listener, GreetListener::class.findAnnotation()!!)

        assertNotNull(Direction.INBOUND.findPacketByClass(GreetPacket::class))
        assertNotNull(Direction.OUTBOUND.findPacketByClass(ByePacket::class))
        assertTrue(Direction.INBOUND.onPacketReceived(GreetPacket("steve"), channel))
        assertTrue(Direction.INBOUND.onPacketReceived(ByePacket("alex"), channel))
        assertEquals(listOf("greet:steve", "bye:alex"), listener.seen)
    }

    @Test
    fun `teardown removes only the handlers of that instance`() = runBlocking {
        val first = GreetListener()
        val second = GreetListener()
        val annotation = GreetListener::class.findAnnotation<PacketListener>()!!
        handler.setup(first, annotation)
        handler.setup(second, annotation)

        handler.teardown(first, annotation)
        Direction.INBOUND.onPacketReceived(GreetPacket("steve"), channel)

        assertEquals(emptyList<String>(), first.seen)
        assertEquals(listOf("greet:steve"), second.seen)
        assertNotNull(Direction.INBOUND.findPacketByClass(GreetPacket::class))
    }

    @Test
    fun `a subscribe function whose first parameter is not a packet is rejected`() {
        val exception = assertThrows(IllegalStateException::class.java) { handler.setup(BrokenListener(), BrokenListener::class.findAnnotation()!!) }

        assertTrue(exception.message!!.contains("noPacket"))
        assertNull(Direction.INBOUND.findPacketByName(String::class.qualifiedName!!))
    }
}
