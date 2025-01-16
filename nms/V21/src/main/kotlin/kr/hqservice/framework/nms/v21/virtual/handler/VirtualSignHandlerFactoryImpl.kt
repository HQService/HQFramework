package kr.hqservice.framework.nms.v21.virtual.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.nms.v21.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket

class VirtualSignHandlerFactoryImpl : VirtualSignHandlerFactory {
    override fun createHandler(
        reflectionWrapper: NmsReflectionWrapper,
        confirmHandler: suspend (List<String>) -> Boolean
    ): VirtualHandler {
        reflectionWrapper as NmsReflectionWrapperImpl

        return object : VirtualHandler {
            override fun getNmsSimpleNames(): List<String> {
                return emptyList()
            }

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
    }
}