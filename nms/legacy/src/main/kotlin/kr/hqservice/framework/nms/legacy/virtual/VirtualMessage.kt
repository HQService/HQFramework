package kr.hqservice.framework.nms.legacy.virtual

interface VirtualMessage {
    fun send(block: (packet: Any) -> Unit)
}