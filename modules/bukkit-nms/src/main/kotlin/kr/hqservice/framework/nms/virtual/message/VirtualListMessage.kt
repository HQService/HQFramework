package kr.hqservice.framework.nms.virtual.message

import kr.hqservice.framework.nms.virtual.VirtualMessage

class VirtualListMessage(
    private val virtualPacket: List<Any>
) : VirtualMessage {
    override suspend fun send(block: suspend (packet: Any) -> Unit) {
        virtualPacket.forEach {
            block(it)
        }
    }
}