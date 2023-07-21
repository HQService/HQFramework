package kr.hqservice.framework.nms.virtual

interface VirtualMessage {
    fun send(block: (packet: Any) -> Unit)
}