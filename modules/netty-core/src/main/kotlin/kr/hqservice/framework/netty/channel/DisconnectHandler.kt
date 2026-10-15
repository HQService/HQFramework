package kr.hqservice.framework.netty.channel

interface DisconnectHandler {
    fun onDisconnect(channel: ChannelWrapper)
}