package kr.hqservice.framework.proxy.core.netty.registry

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.netty.channel.Channel
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.channel.ChannelConnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelDisconnectedPacket
import kr.hqservice.framework.netty.packet.server.ShutdownPacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AbstractNettyChannelRegistryTest {
    private class FakeBackend(port: Int) {
        val channel = mockk<Channel>(relaxed = true)
        val disconnectHandler = slot<(ChannelWrapper) -> Unit>()
        val wrapper = mockk<ChannelWrapper>(relaxed = true)

        init {
            val bossHandler = mockk<BossHandler>(relaxed = true)
            every { bossHandler.setDisconnectionHandler(capture(disconnectHandler)) } returns Unit
            every { channel.pipeline().get(BossHandler::class.java) } returns bossHandler
            every { wrapper.channel } returns channel
            every { wrapper.port } returns port
        }

        fun goInactive() = disconnectHandler.captured(wrapper)
    }

    private class TestRegistry(
        override val registersBeforeAnnouncing: Boolean
    ) : AbstractNettyChannelRegistry(mockk<HQYamlConfiguration>(relaxed = true)) {
        val connected = mutableListOf<String>()
        val disconnected = mutableListOf<ChannelWrapper>()

        override fun resolveServerName(port: Int): String? = if (port == LOBBY_PORT) "lobby" else null

        override fun fireConnected(wrapper: ChannelWrapper, name: String) {
            connected.add(name)
        }

        override fun fireDisconnected(wrapper: ChannelWrapper) {
            disconnected.add(wrapper)
        }

        override fun firePacketReceived(packet: Packet, wrapper: ChannelWrapper) {}

        override fun runAsync(task: () -> Unit) = task()

        override fun deliver(wrapper: ChannelWrapper, packet: Packet) {
            wrapper.sendPacket(packet)
        }

        override fun sendShutdown(wrapper: ChannelWrapper, packet: ShutdownPacket) {}

        override fun collectPlayers(connectedChannels: List<NettyChannel>): MutableList<NettyPlayer> = mutableListOf()
    }

    private val registry = TestRegistry(registersBeforeAnnouncing = false)

    @Test
    fun `reconnect on the same port closes the previous channel`() {
        val old = FakeBackend(LOBBY_PORT)
        val new = FakeBackend(LOBBY_PORT)

        registry.registerActiveChannel(LOBBY_PORT, old.wrapper)
        registry.registerActiveChannel(LOBBY_PORT, new.wrapper)

        verify { old.channel.close() }
        assertSame(new.wrapper, registry.getChannelByPort(LOBBY_PORT))
        assertSame(new.wrapper, registry.getChannelByServerName("lobby"))
        assertEquals(listOf(new.wrapper), registry.getChannels())
    }

    @Test
    fun `stale channel going inactive leaves the newer channel registered`() {
        val old = FakeBackend(LOBBY_PORT)
        val new = FakeBackend(LOBBY_PORT)
        registry.registerActiveChannel(LOBBY_PORT, old.wrapper)
        registry.registerActiveChannel(LOBBY_PORT, new.wrapper)

        old.goInactive()

        assertSame(new.wrapper, registry.getChannelByPort(LOBBY_PORT))
        assertSame(new.wrapper, registry.getChannelByServerName("lobby"))
        verify(exactly = 0) { new.wrapper.sendPacket(ofType<ChannelDisconnectedPacket>()) }
        assertEquals(emptyList<ChannelWrapper>(), registry.disconnected)
    }

    @Test
    fun `current channel going inactive unregisters it and notifies the others`() {
        val lobby = FakeBackend(LOBBY_PORT)
        val other = FakeBackend(30000)
        registry.registerActiveChannel(LOBBY_PORT, lobby.wrapper)
        registry.registerActiveChannel(30000, other.wrapper)

        lobby.goInactive()

        assertFailsWith<IllegalArgumentException> { registry.getChannelByPort(LOBBY_PORT) }
        assertFailsWith<IllegalArgumentException> { registry.getChannelByServerName("lobby") }
        verify { other.wrapper.sendPacket(ofType<ChannelDisconnectedPacket>()) }
        assertEquals(listOf(lobby.wrapper), registry.disconnected)
    }

    @Test
    fun `unknown port has a placeholder name instead of throwing`() {
        assertEquals("Unknown-1234", registry.getChannelNameByPort(1234))
    }

    @Test
    fun `unresolved backends get sequential unknown names`() {
        registry.registerActiveChannel(30000, FakeBackend(30000).wrapper)
        registry.registerActiveChannel(30001, FakeBackend(30001).wrapper)

        assertEquals(listOf("Unknown-1", "Unknown-2"), registry.connected)
    }

    @Test
    fun `announcing before registering does not notify the new channel of itself`() {
        val lobby = FakeBackend(LOBBY_PORT)

        registry.registerActiveChannel(LOBBY_PORT, lobby.wrapper)

        verify(exactly = 0) { lobby.wrapper.sendPacket(ofType<ChannelConnectedPacket>()) }
    }

    @Test
    fun `registering before announcing notifies the new channel of itself`() {
        val registry = TestRegistry(registersBeforeAnnouncing = true)
        val lobby = FakeBackend(LOBBY_PORT)

        registry.registerActiveChannel(LOBBY_PORT, lobby.wrapper)

        verify(exactly = 1) { lobby.wrapper.sendPacket(ofType<ChannelConnectedPacket>()) }
    }

    private companion object {
        const val LOBBY_PORT = 25565
    }
}
