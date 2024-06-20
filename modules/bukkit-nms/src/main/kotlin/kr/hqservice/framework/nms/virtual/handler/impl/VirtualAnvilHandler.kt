package kr.hqservice.framework.nms.virtual.handler.impl

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.callAccess
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin

class VirtualAnvilHandler(
    private val reflectionWrapper: NmsReflectionWrapper,
    private val textScope: suspend (String) -> Unit,
    private val confirmScope: suspend (String) -> Boolean,
    private val buttonScope: suspend (Int, String) -> Boolean,
    private val otherSlotClickScope: suspend () -> Unit,
    private val dummyListener: VirtualAnvilListener,
    private val closeScope: suspend (String) -> Unit
) : VirtualHandler {
    class VirtualAnvilListener(
        private val player: Player,
        private val plugin: Plugin
    ) : Listener {
        fun close() {
            plugin.getScheduler().runTask {
                InventoryClickEvent.getHandlerList().unregister(this)
                player.updateInventory()
            }
        }
    }

    private var currentText = ""
    private var unregistered = false

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
            runBlocking { closeScope.invoke(currentText) }
            dummyListener.close()
            return true
        }
        return false
    }

    override fun handle(message: Any) {
        val clazz = message::class
        when (clazz.simpleName!!) {
            "PacketPlayInItemName" -> {
                val nameField = reflectionWrapper.getField(message::class, "name",
                    Version.V_17.handle("a"),
                    Version.V_17_FORGE.handle("f_134393_")
                )
                val name = nameField.callAccess<String>(message)
                runBlocking { textScope(name) }
                currentText = name
            }
            "PacketPlayInWindowClick" -> {
                val slotNumField = reflectionWrapper.getField(message::class, "slotNum",
                    Version.V_17.handle("d"),
                    Version.V_17_FORGE.handle("f_133940_")
                )
                val slotNum = slotNumField.callAccess<Int>(message)
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