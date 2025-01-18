package kr.hqservice.framework.nms.legacy.virtual.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.callAccess
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper

class LegacyVirtualSignHandlerFactory : VirtualSignHandlerFactory {
    override fun createHandler(
        reflectionWrapper: NmsReflectionWrapper,
        confirmHandler: suspend (List<String>) -> Boolean
    ): VirtualHandler {
        reflectionWrapper as LegacyNmsReflectionWrapper

        return object : VirtualHandler {
            override fun getNmsSimpleNames(): List<String> {
                return listOf("PacketPlayInUpdateSign")
            }

            override fun checkCondition(message: Any): Boolean {
                return getNmsSimpleNames().contains(message::class.simpleName)
            }

            override fun unregisterType(): HandlerUnregisterType {
                return HandlerUnregisterType.ALL
            }

            override fun unregisterCondition(message: Any): Boolean {
                if (message::class.simpleName == "PacketPlayInUpdateSign") {
                    val linesField = reflectionWrapper.getField(message::class, "lines",
                        Version.V_17.handle("c"),
                        Version.V_17_FORGE.handle("f_134646_")
                    )
                    val texts = linesField.callAccess<Array<String>>(message).toList()
                    runBlocking { confirmHandler(texts) }
                    return true
                }
                return false
            }

            override fun handle(message: Any) {}
        }
    }
}