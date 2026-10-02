package kr.hqservice.framework.netty.api

interface NettyChannel {
    fun getName(): String

    fun getPort(): Int
}