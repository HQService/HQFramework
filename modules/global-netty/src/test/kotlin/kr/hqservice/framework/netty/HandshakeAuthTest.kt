package kr.hqservice.framework.netty

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.embedded.EmbeddedChannel
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.ConnectionState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.logging.Logger

class HandshakeAuthTest {
    private lateinit var handler: BossHandler

    @BeforeEach
    fun register() {
        Direction.INBOUND.registerPacket(HandShakePacket::class)
    }

    @AfterEach
    fun unregister() {
        Direction.INBOUND.unregisterPacket(HandShakePacket::class)
    }

    private fun channelExpecting(secret: String?) = EmbeddedChannel(object : ChannelInitializer<Channel>() {
        override fun initChannel(ch: Channel) {
            handler = BossHandler(ch, Logger.getAnonymousLogger(), secret)
            ch.pipeline().addLast(handler)
        }
    })

    @Test
    fun `wrong secret closes the channel without connecting`() {
        val channel = channelExpecting("s3cret")

        channel.writeInbound(HandShakePacket(25565, "wrong"))

        assertFalse(channel.isOpen)
        assertEquals(ConnectionState.IDLE, handler.connectionState)
    }

    @Test
    fun `right secret connects before writeInbound returns`() {
        val channel = channelExpecting("s3cret")

        channel.writeInbound(HandShakePacket(25565, "s3cret"))

        assertTrue(channel.isOpen)
        assertEquals(ConnectionState.CONNECTED, handler.connectionState)
    }

    @Test
    fun `client side accepts any secret`() {
        val channel = channelExpecting(null)

        channel.writeInbound(HandShakePacket(-1, "anything"))

        assertEquals(ConnectionState.CONNECTED, handler.connectionState)
    }
}
