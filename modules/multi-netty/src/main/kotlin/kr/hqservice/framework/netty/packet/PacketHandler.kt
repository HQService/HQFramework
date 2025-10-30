package kr.hqservice.framework.netty.packet

import kr.hqservice.framework.netty.channel.ChannelWrapper

interface PacketHandler<T : Packet> {
    suspend fun onPacketReceive(packet: T, channel: ChannelWrapper)
}