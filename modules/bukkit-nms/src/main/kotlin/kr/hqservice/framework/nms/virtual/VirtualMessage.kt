package kr.hqservice.framework.nms.virtual

interface VirtualMessage {
    suspend fun send(block: suspend (packet: Any) -> Unit)
}