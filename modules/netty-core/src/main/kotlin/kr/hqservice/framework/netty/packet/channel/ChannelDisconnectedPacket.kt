package kr.hqservice.framework.netty.packet.channel

import io.netty.buffer.ByteBuf
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.extension.readChannel
import kr.hqservice.framework.netty.packet.extension.writeChannel

class ChannelDisconnectedPacket(
    var channel: NettyChannel
) : Packet() {
    override fun write(buf: ByteBuf) {
        buf.writeChannel(channel)
    }

    override fun read(buf: ByteBuf) {
        channel = buf.readChannel()!!
    }
}