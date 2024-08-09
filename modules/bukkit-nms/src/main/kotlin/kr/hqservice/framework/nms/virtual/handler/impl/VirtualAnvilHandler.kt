package kr.hqservice.framework.nms.virtual.handler.impl

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin

class VirtualAnvilHandler(
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
                val slotNum = message.slotNum
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