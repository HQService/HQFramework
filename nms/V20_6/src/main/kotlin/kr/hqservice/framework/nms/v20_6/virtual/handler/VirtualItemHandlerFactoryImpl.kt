package kr.hqservice.framework.nms.v20_6.virtual.handler

import kr.hqservice.framework.nms.extension.getNmsItemStack
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.v20_6.wrapper.reflect.NmsReflectionWrapperImpl
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.inventory.ItemStack

class VirtualItemHandlerFactoryImpl : VirtualItemHandlerFactory {
    override fun createHandler(
        itemStackService: NmsItemStackService,
        reflectionWrapper: NmsReflectionWrapper,
        targetContainer: Int,
        filter: (Int, ItemStack) -> Boolean,
        item: (index: Int, itemStack: ItemStack) -> Unit
    ): VirtualHandler {
        reflectionWrapper as NmsReflectionWrapperImpl

        return object : VirtualHandler {
            override fun getNmsSimpleNames(): List<String> {
                return emptyList()
            }

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
    }
}