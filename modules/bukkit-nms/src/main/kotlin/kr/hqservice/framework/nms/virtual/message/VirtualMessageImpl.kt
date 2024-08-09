package kr.hqservice.framework.nms.virtual.message

import kr.hqservice.framework.nms.virtual.VirtualMessage
import net.minecraft.network.protocol.Packet

class VirtualMessageImpl(
    internal val virtualPacket: Packet<*>
) : VirtualMessage {
    override fun send(block: (packet: Packet<*>) -> Unit) {
        block(virtualPacket)
    }
}