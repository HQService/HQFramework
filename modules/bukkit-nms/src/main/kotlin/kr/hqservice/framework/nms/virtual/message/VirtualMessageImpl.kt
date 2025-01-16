package kr.hqservice.framework.nms.virtual.message

import kr.hqservice.framework.nms.virtual.VirtualMessage

class VirtualMessageImpl(
    private val virtualPacket: Any
) : VirtualMessage {
    override fun send(block: (packet: Any) -> Unit) {
        block(virtualPacket)
    }
}