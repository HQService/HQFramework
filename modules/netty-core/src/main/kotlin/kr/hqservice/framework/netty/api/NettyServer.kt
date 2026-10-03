package kr.hqservice.framework.netty.api

import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import java.util.*
import kotlin.reflect.KClass

interface NettyServer {
    fun getChannels(): List<NettyChannel>

    fun getChannel(name: String): NettyChannel?

    fun getChannel(port: Int): NettyChannel?

    fun getPlayer(name: String): NettyPlayer?

    fun getPlayer(uniqueId: UUID): NettyPlayer?

    fun getPlayers(): List<NettyPlayer>

    fun getPlayers(channel: NettyChannel): List<NettyPlayer>

    @Deprecated("보낼 패킷은 첫 전송 때 자동 등록됩니다. 받을 패킷과 함께 선언하려면 @PacketListener(outbound = [...]) 를 쓰세요")
    fun <T : Packet> registerOuterPacket(packetClass: KClass<T>)

    @Deprecated("@PacketListener 클래스의 @PacketSubscribe 함수로 받으세요. 플러그인 disable 시 자동으로 해제됩니다")
    fun <T : Packet> registerInnerPacket(
        packetClass: KClass<T>,
        packetHandler: (packet: T, channel: ChannelWrapper) -> Unit,
    )

    @Deprecated("@PacketListener 클래스의 @PacketSubscribe 함수로 받으세요. 플러그인 disable 시 자동으로 해제됩니다")
    fun <T : Packet> registerInnerPacket(
        packetClass: KClass<T>,
        packetHandler: PacketHandler<T>,
    )
}