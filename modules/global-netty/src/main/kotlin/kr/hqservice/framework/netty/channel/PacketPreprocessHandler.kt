package kr.hqservice.framework.netty.channel

import kr.hqservice.framework.netty.packet.AbstractPacket

interface PacketPreprocessHandler {
    fun preprocess(packet: AbstractPacket, channel: ChannelWrapper)
}