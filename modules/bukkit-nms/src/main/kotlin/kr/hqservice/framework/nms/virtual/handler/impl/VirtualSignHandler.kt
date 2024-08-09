package kr.hqservice.framework.nms.virtual.handler.impl

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket

class VirtualSignHandler(
    private val confirmHandler: suspend (List<String>) -> Boolean
) : VirtualHandler {
    override fun checkCondition(message: Any): Boolean {
        return message is ServerboundSignUpdatePacket
    }

    override fun unregisterType(): HandlerUnregisterType {
        return HandlerUnregisterType.ALL
    }

    override fun unregisterCondition(message: Any): Boolean {
        if (message is ServerboundSignUpdatePacket) {
            val lines = message.lines.toList()
            runBlocking { confirmHandler(lines) }
            return true
        }
        return false
    }

    override fun handle(message: Any) {}
}