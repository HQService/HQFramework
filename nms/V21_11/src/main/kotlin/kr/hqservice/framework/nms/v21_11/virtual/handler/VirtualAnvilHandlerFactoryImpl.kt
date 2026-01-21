package kr.hqservice.framework.nms.v21_11.virtual.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.nms.v21_11.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.handler.AnvilDummyListener
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class VirtualAnvilHandlerFactoryImpl : VirtualAnvilHandlerFactory {
    override fun createListener(player: Player, plugin: Plugin): AnvilDummyListener {
        val listener = AnvilDummyListenerImpl(player, plugin)
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
        reflectionWrapper as NmsReflectionWrapperImpl

        return object : VirtualHandler {
            private var currentText = ""
            private var unregistered = false

            override fun getNmsSimpleNames(): List<String> = emptyList()

            override fun checkCondition(message: Any): Boolean {
                return message is ServerboundRenameItemPacket || message is ServerboundContainerClickPacket
            }

            override fun unregisterType(): HandlerUnregisterType {
                return HandlerUnregisterType.ALL
            }

            override fun unregisterCondition(message: Any): Boolean {
                if (unregistered || message is ServerboundContainerClosePacket) {
                    runBlocking { closeScope.invoke(currentText) }
                    dummyListener.close()
                    return true
                }
                return false
            }

            override fun handle(message: Any) {
                when (message) {
                    is ServerboundRenameItemPacket -> {
                        val name = message.name
                        runBlocking { textScope(name) }
                        currentText = name
                    }

                    is ServerboundContainerClickPacket -> {
                        val slotNum = message.slotNum.toInt()
                        if (slotNum == 2) {
                            runBlocking {
                                if (confirmScope(currentText)) {
                                    unregistered = true
                                } else if (buttonScope(2, currentText)) {
                                    unregistered = true
                                }
                            }
                        } else if (slotNum in 0..1) {
                            runBlocking {
                                if (buttonScope(slotNum, currentText)) {
                                    unregistered = true
                                } else otherSlotClickScope()
                            }
                        }
                    }
                }
            }
        }
    }
}