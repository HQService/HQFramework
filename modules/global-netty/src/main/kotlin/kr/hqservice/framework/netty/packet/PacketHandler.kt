package kr.hqservice.framework.netty.packet

import kr.hqservice.framework.netty.channel.ChannelWrapper

interface PacketHandler<T : AbstractPacket> {
    fun onPacketReceive(packet: T, channel: ChannelWrapper)
}