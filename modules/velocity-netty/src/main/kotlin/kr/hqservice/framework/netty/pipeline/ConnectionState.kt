package kr.hqservice.framework.netty.pipeline

enum class ConnectionState(
    val id: Int
) {

    IDLE(0),
    HANDSHAKING(1),
    CONNECTED(2)

}