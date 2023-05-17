package kr.hqservice.framework.netty.packet

import io.netty.buffer.ByteBuf

abstract class AbstractPacket {
    private var callbackResult = false

    fun isCallbackResult(): Boolean {
        return callbackResult
    }

    fun setCallbackResult(callbackResult: Boolean) {
        this.callbackResult = callbackResult
    }

    abstract fun write(buf: ByteBuf)

    abstract fun read(buf: ByteBuf)
}