package kr.hqservice.framework.netty.api

interface NettyChannel : MessageSender {
    fun getName(): String

    fun getPort(): Int
}