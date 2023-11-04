package kr.hqservice.framework.nms.virtual.handler.impl

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.nms.extension.callAccess
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper

class VirtualAnvilHandler(
    private val reflectionWrapper: NmsReflectionWrapper,
    private val textScope: suspend (String) -> Unit,
    private val confirmScope: suspend (String) -> Unit,
    private val otherSlotClickScope: suspend () -> Unit
) : VirtualHandler {
    private var currentText = ""

    override fun getNmsSimpleNames(): List<String> {
        return listOf("PacketPlayInItemName", "PacketPlayInWindowClick")
    }

    override fun checkCondition(message: Any): Boolean {
        return getNmsSimpleNames().contains(message::class.simpleName)
    }

    override fun unregisterType(): HandlerUnregisterType {
        return HandlerUnregisterType.ALL
    }

    override fun unregisterCondition(message: Any): Boolean {
        return message::class.simpleName == "PacketPlayInCloseWindow"
    }

    override fun handle(message: Any) {
        val clazz = message::class
        when (clazz.simpleName!!) {
            "PacketPlayInItemName" -> {
                val nameField = reflectionWrapper.getField(message::class, "a")
                val name = nameField.callAccess<String>(message)
                runBlocking { textScope(name) }
                currentText = name
            }
            "PacketPlayInWindowClick" -> {
                val slotNumField = reflectionWrapper.getField(message::class, "d")
                val slotNum = slotNumField.callAccess<Int>(message)
                if (slotNum == 2) {
                    runBlocking { confirmScope(currentText) }
                } else if (slotNum in 0..1) {
                    runBlocking { otherSlotClickScope() }
                }
            }
        }
    }
}