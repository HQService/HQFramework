package kr.hqservice.netty.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.MockPlugin
import be.seeseemelk.mockbukkit.ServerMock
import io.netty.handler.codec.EncoderException
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import kr.hqservice.netty.test.global.TestBootstrap
import kr.hqservice.framework.yaml.extension.yaml
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NettyPacketTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: MockPlugin

    @BeforeEach
    fun setup() {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()
    }

    @Test
    fun netty_inbound_test() {
        val config = File("src/test/resources/config.yml").yaml()
        val channel = TestBootstrap(plugin.logger, config).initTestChannel()

        Direction.INBOUND.addListener(HandShakePacket::class) { packet, wrapper ->
            wrapper.port = packet.port
            wrapper.handler.setConnectionState(ConnectionState.CONNECTED)
            println("received handshake packet ${packet.port}")
            assertEquals(wrapper.port, 25545)
        }

        channel.writeInbound(HandShakePacket(25545))
        channel.finish()
    }

    @Test
    fun netty_outbound_test() {
        val config = File("src/test/resources/config.yml").yaml()
        val channel = TestBootstrap(plugin.logger, config).initTestChannel()

        // unregistered packet -> throw
        assertThrows<EncoderException> {
            channel.writeOutbound(HandShakePacket(25545))
        }

        Direction.OUTBOUND.registerPacket(HandShakePacket::class)

        channel.writeOutbound(HandShakePacket(25545))
        channel.finish()
    }

    @AfterEach
    fun teardown() {
        MockBukkit.unmock()
    }

}