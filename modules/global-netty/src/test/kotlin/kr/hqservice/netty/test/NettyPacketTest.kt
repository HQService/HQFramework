package kr.hqservice.netty.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.MockPlugin
import be.seeseemelk.mockbukkit.ServerMock
import io.netty.handler.codec.EncoderException
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import kr.hqservice.framework.netty.packet.server.PingPongPacket
import kr.hqservice.framework.netty.pipeline.ConnectionState
import kr.hqservice.framework.yaml.extension.yaml
import kr.hqservice.netty.test.global.TestBootstrap
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.MethodCall
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import kotlin.reflect.full.primaryConstructor

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

        Direction.INBOUND.registerPacket(HandShakePacket::class)
        Direction.INBOUND.addListener(HandShakePacket::class) { packet, wrapper ->
            wrapper.port = packet.port
            wrapper.handler.setConnectionState(ConnectionState.CONNECTED)
            println("received handshake packet ${packet.port}")
            assertEquals(wrapper.port, 25545)
        }

        channel.writeInbound(HandShakePacket(25545))
        channel.finish()
    }

    /*@Test
    fun netty_vjh_() {
        ByteBuddyAgent.install()
        val packetName = HandShakePacket::class.qualifiedName!! + "\$vjh"

        val clazz: Class<*> = ByteBuddy()
            .subclass(HandShakePacket::class.java)
            .name(packetName)
            .defineConstructor(Visibility.PUBLIC)
            .intercept(
                MethodCall.invoke(HandShakePacket::class.java.getConstructor(Int::class.java))
                    .with(0))
            .make()
            .load(HandShakePacket::class.java.classLoader, ClassLoadingStrategy.Default.WRAPPER)
            .loaded

        val hand = clazz.getConstructor().newInstance().apply {
            val method = clazz.getMethod("setPort", Int::class.java)
            method.invoke(this, 20)
        } as HandShakePacket
        assertEquals(hand.port, 20)
    }*/

    @Test
    fun netty_vjh() {
        val targetClass = PingPongPacket::class
        val packetName = targetClass.qualifiedName!! + "\$vjh"

        val clazz: Class<*> = ByteBuddy()
            .redefine(targetClass.java)
            .name(packetName)
            .defineConstructor(Visibility.PUBLIC)
            .intercept(MethodCall.invokeSuper())
            .make()
            .load(targetClass.java.classLoader)
            .loaded

        val hand = clazz.getConstructor().newInstance().apply {
            val method = clazz.getMethod("setReceivedTime", Long::class.java)
            method.invoke(this, 55)
        }

        val constructor = targetClass.primaryConstructor!!
        val params = mutableListOf<Any?>()

        constructor.parameters.forEach {
            val field = clazz.getDeclaredField(it.name!!)
            field.isAccessible = true
            params.add(field.get(hand).print("field ${it.name} value: "))
            field.isAccessible = false
        }

        val handShakePacket = constructor.call(*params.toTypedArray())
        println(handShakePacket.receivedTime)
        println(handShakePacket.time)
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