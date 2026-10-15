package kr.hqservice.framework.netty

import io.netty.buffer.Unpooled
import io.netty.handler.codec.DecoderException
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyChannelImpl
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.netty.packet.extension.readChannel
import kr.hqservice.framework.netty.packet.extension.readPlayer
import kr.hqservice.framework.netty.packet.extension.readPlayers
import kr.hqservice.framework.netty.packet.extension.writeChannel
import kr.hqservice.framework.netty.packet.extension.writePlayer
import kr.hqservice.framework.netty.packet.extension.writePlayers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.UUID

class PlayerBlobTest {
    private val trailer = 0x7E57

    private fun player(index: Int): NettyPlayer = NettyPlayerImpl(
        "player$index",
        "display $index",
        UUID.randomUUID(),
        if (index % 2 == 0) NettyChannelImpl(25565 + index, "lobby-$index") else null
    )

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 3])
    fun `player list survives a round trip and leaves the following bytes intact`(count: Int) {
        val players = List(count, ::player)
        val buf = Unpooled.buffer()
        buf.writePlayers(players)
        buf.writeInt(trailer)

        assertEquals(players, buf.readPlayers())
        assertEquals(trailer, buf.readInt())
    }

    @Test
    fun `single player with and without a channel survives a round trip`() {
        val players = listOf(player(0), player(1))
        val buf = Unpooled.buffer()
        players.forEach(buf::writePlayer)
        buf.writeInt(trailer)

        assertEquals(players, List(2) { buf.readPlayer() })
        assertEquals(trailer, buf.readInt())
    }

    @Test
    fun `null channel survives a round trip`() {
        val buf = Unpooled.buffer()
        buf.writeChannel(null)
        buf.writeInt(trailer)

        assertNull(buf.readChannel())
        assertEquals(trailer, buf.readInt())
    }

    @Test
    fun `corrupt player payload is rejected`() {
        val buf = Unpooled.buffer()
        buf.writeInt(3)
        buf.writeBytes(byteArrayOf(9, 9, 9))

        assertThrows<DecoderException> { buf.readPlayers() }
    }
}
