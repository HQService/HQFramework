package kr.hqservice.framework.netty.packet.player

enum class PlayerConnectionState {
    SWITCHED_CHANNEL,
    PRE_SWITCH_CHANNEL,
    CONNECTED,
    PRE_CONNECT,
    DISCONNECT
}