package kr.hqservice.framework.nms.legacy.virtual.handler

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.callAccess
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.handler.AnvilDummyListener
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.handler.launchVirtualCallback
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class LegacyVirtualAnvilHandlerFactory : VirtualAnvilHandlerFactory {
    override fun createListener(player: Player, plugin: Plugin): AnvilDummyListener {
        val listener = LegacyAnvilDummyListener(player, plugin)
        return listener
    }

    override fun createHandler(
        reflectionWrapper: NmsReflectionWrapper,
        textScope: suspend (String) -> Unit,
        confirmScope: suspend (String) -> Boolean,
        buttonScope: suspend (Int, String) -> Boolean,
        otherSlotClickScope: suspend () -> Unit,
        dummyListener: AnvilDummyListener,
        closeScope: suspend (String) -> Unit
    ): VirtualHandler {
        reflectionWrapper as LegacyNmsReflectionWrapper

        return object : VirtualHandler {
            private var currentText = ""
            @Volatile
            private var unregistered = false
            @Volatile
            private var resolving = false

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
                if (unregistered || message::class.simpleName == "PacketPlayInCloseWindow") {
                    val text = currentText
                    launchVirtualCallback { closeScope(text) }
                    dummyListener.close()
                    return true
                }
                return false
            }

            override fun close() {
                dummyListener.close()
            }

            override fun handle(message: Any) {
                val clazz = message::class
                when (clazz.simpleName!!) {
                    "PacketPlayInItemName" -> {
                        val nameField = reflectionWrapper.getField(
                            message::class, "name",
                            Version.V_17.handle("a"),
                            Version.V_17_FORGE.handle("f_134393_")
                        )
                        val name = nameField.callAccess<String>(message)
                        launchVirtualCallback { textScope(name) }
                        currentText = name
                    }

                    "PacketPlayInWindowClick" -> {
                        val slotNumField = reflectionWrapper.getField(
                            message::class, "slotNum",
                            Version.V_17.handle("d"),
                            Version.V_17_FORGE.handle("f_133940_")
                        )
                        val slotNum = slotNumField.callAccess<Int>(message)
                        if (slotNum == 2) {
                            if (resolving) return
                            resolving = true
                            val text = currentText
                            launchVirtualCallback {
                                try {
                                    if (confirmScope(text)) {
                                        unregistered = true
                                    } else if (buttonScope(2, text)) {
                                        unregistered = true
                                    }
                                } finally {
                                    if (!unregistered) resolving = false
                                }
                            }
                        } else if (slotNum in 0..1) {
                            if (resolving) return
                            resolving = true
                            val text = currentText
                            launchVirtualCallback {
                                try {
                                    if (buttonScope(slotNum, text)) {
                                        unregistered = true
                                    } else otherSlotClickScope()
                                } finally {
                                    if (!unregistered) resolving = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}