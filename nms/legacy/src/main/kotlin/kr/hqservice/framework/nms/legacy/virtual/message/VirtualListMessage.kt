package kr.hqservice.framework.nms.legacy.virtual.message

import kr.hqservice.framework.nms.virtual.VirtualMessage

class VirtualListMessage(
    private val virtualPacket: List<Any>
) : VirtualMessage {
    override fun send(block: (packet: Any) -> Unit) {
        virtualPacket.forEach(block)
    }
}