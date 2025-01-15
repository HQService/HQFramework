package kr.hqservice.framework.nms.legacy.virtual.message

import kr.hqservice.framework.nms.virtual.VirtualMessage

class VirtualMessageImpl(
    internal val virtualPacket: Any
) : VirtualMessage {
    override fun send(block: (packet: Any) -> Unit) {
        block(virtualPacket)
    }
}