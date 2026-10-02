package kr.hqservice.framework.netty.packet.server

import io.netty.buffer.Unpooled
import kr.hqservice.framework.netty.packet.channel.ChannelConnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelDisconnectedPacket
import kr.hqservice.framework.netty.packet.channel.ChannelListPacket
import kr.hqservice.framework.netty.packet.extension.readString
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket

object RelayPolicy {
    private val serverOnly = setOf(
        ShutdownPacket::class, HandShakePacket::class, ChannelListPacket::class, ChannelConnectedPacket::class,
        ChannelDisconnectedPacket::class, PlayerConnectionPacket::class, PingPongPacket::class,
        RelayingPacket::class, RelayingResult::class
    ).map { it.qualifiedName!! }.toSet()

    fun isRelayable(relay: ByteArray): Boolean {
        val buf = Unpooled.wrappedBuffer(relay)
        return try {
            buf.readString() !in serverOnly
        } catch (_: RuntimeException) {
            false
        } finally {
            buf.release()
        }
    }
}
