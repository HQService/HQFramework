package kr.hqservice.framework.bungee.core.netty.registry

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.netty.channel.Channel
import kr.hqservice.framework.bungee.core.netty.registry.impl.NettyChannelRegistryImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.channel.ChannelDisconnectedPacket
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class NettyChannelRegistryImplTest {
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

    private val lobbyPort = 25565
    private val proxy = mockk<ProxyServer>(relaxed = true).also { proxy ->
        val lobby = mockk<ServerInfo>(relaxed = true)
        every { lobby.name } returns "lobby"
        every { lobby.address } returns InetSocketAddress("127.0.0.1", lobbyPort)
        every { proxy.servers } returns mapOf("lobby" to lobby)
        every { proxy.players } returns emptyList()
    }
    private val registry = NettyChannelRegistryImpl(proxy, mockk<HQYamlConfiguration>(relaxed = true))

    @Test
    fun `reconnect on the same port closes the previous channel`() {
        val old = FakeBackend(lobbyPort)
        val new = FakeBackend(lobbyPort)

        registry.registerActiveChannel(lobbyPort, old.wrapper)
        registry.registerActiveChannel(lobbyPort, new.wrapper)

        verify { old.channel.close() }
        assertSame(new.wrapper, registry.getChannelByPort(lobbyPort))
        assertSame(new.wrapper, registry.getChannelByServerName("lobby"))
        assertEquals(listOf(new.wrapper), registry.getChannels())
    }

    @Test
    fun `stale channel going inactive leaves the newer channel registered`() {
        val old = FakeBackend(lobbyPort)
        val new = FakeBackend(lobbyPort)
        registry.registerActiveChannel(lobbyPort, old.wrapper)
        registry.registerActiveChannel(lobbyPort, new.wrapper)

        old.goInactive()

        assertSame(new.wrapper, registry.getChannelByPort(lobbyPort))
        assertSame(new.wrapper, registry.getChannelByServerName("lobby"))
        verify(exactly = 0) { new.wrapper.sendPacket(ofType<ChannelDisconnectedPacket>()) }
    }

    @Test
    fun `current channel going inactive unregisters it and notifies the others`() {
        val lobby = FakeBackend(lobbyPort)
        val other = FakeBackend(30000)
        registry.registerActiveChannel(lobbyPort, lobby.wrapper)
        registry.registerActiveChannel(30000, other.wrapper)

        lobby.goInactive()

        assertFailsWith<IllegalArgumentException> { registry.getChannelByPort(lobbyPort) }
        assertFailsWith<IllegalArgumentException> { registry.getChannelByServerName("lobby") }
        verify { other.wrapper.sendPacket(ofType<ChannelDisconnectedPacket>()) }
    }

    @Test
    fun `unknown port has a placeholder name instead of throwing`() {
        assertEquals("Unknown-1234", registry.getChannelNameByPort(1234))
    }
}
