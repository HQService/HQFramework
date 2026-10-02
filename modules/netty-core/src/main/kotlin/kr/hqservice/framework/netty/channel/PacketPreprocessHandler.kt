package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.Packet

interface PacketPreprocessHandler {
    fun preprocess(packet: Packet, channel: ChannelWrapper)
}