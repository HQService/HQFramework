package kr.hqservice.framework.bungee.core.netty.registry

import io.mockk.every
import io.mockk.mockk
import io.netty.channel.Channel
import kr.hqservice.framework.bungee.core.netty.registry.impl.NettyChannelRegistryImpl
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertSame

class NettyChannelRegistryImplTest {
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
    fun `backend name is resolved from the bungee server list by port`() {
        val channel = mockk<Channel>(relaxed = true)
        every { channel.pipeline().get(BossHandler::class.java) } returns mockk(relaxed = true)
        val wrapper = mockk<ChannelWrapper>(relaxed = true)
        every { wrapper.channel } returns channel
        every { wrapper.port } returns lobbyPort

        registry.registerActiveChannel(lobbyPort, wrapper)

        assertSame(wrapper, registry.getChannelByServerName("lobby"))
    }
}
