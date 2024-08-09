package kr.hqservice.framework.nms.virtual.message

import kr.hqservice.framework.nms.virtual.VirtualMessage
import net.minecraft.network.protocol.Packet

class VirtualListMessage(
    private val virtualPacket: List<Packet<*>>
) : VirtualMessage {
    override fun send(block: (packet: Packet<*>) -> Unit) {
        virtualPacket.forEach(block)
    }
}