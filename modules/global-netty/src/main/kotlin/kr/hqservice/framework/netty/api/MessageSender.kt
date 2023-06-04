package kr.hqservice.framework.netty.api

interface MessageSender {
    fun sendMessage(message: String, logging: Boolean = true)
}