package kr.hqservice.framework.nms.virtual.handler.impl

import kr.hqservice.framework.nms.extension.getNmsItemStack
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.inventory.ItemStack

class VirtualItemHandler(
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper>,
    private val targetContainer: Int,
    private var filter: (Int, ItemStack) -> Boolean,
    private var item: (index: Int, itemStack: ItemStack) -> Unit
) : VirtualHandler {
    override fun checkCondition(message: Any): Boolean {
        return if (message is ClientboundContainerSetContentPacket) {
            targetContainer == message.containerId
        } else false
    }

    override fun unregisterType(): HandlerUnregisterType {
        return HandlerUnregisterType.ALL
    }

    override fun unregisterCondition(message: Any): Boolean {
        return when (message) {
            is ClientboundOpenScreenPacket -> {
                targetContainer != message.containerId
            }
            is ClientboundContainerClosePacket -> {
                targetContainer == message.containerId
            }
            else -> false
        }
    }

    override fun handle(message: Any) {
        when (message) {
            is ClientboundContainerSetContentPacket -> {
                val list = message.items
                list.forEachIndexed { index, itemStack ->
                    val wrapper = itemStackService.getWrapper(itemStack)
                    val bukkitItemStack = wrapper.getBukkitItemStack()
                    if (filter(index, bukkitItemStack)) {
                        list[index] = bukkitItemStack.apply { item(index, bukkitItemStack) }.getNmsItemStack()
                            .getUnwrappedInstance() as net.minecraft.world.item.ItemStack
                    }
                }
            }
        }
    }
}