package kr.hqservice.framework.nms.legacy.virtual.handler

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.callAccess
import kr.hqservice.framework.nms.extension.getNmsItemStack
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.virtual.handler.HandlerUnregisterType
import kr.hqservice.framework.nms.virtual.handler.VirtualHandler
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.inventory.ItemStack

class LegacyVirtualItemHandlerFactory : VirtualItemHandlerFactory {
    override fun createHandler(
        itemStackService: NmsItemStackService,
        reflectionWrapper: NmsReflectionWrapper,
        targetContainer: Int,
        filter: (Int, ItemStack) -> Boolean,
        item: (index: Int, itemStack: ItemStack) -> Unit
    ): VirtualHandler {
        reflectionWrapper as LegacyNmsReflectionWrapper

        return object : VirtualHandler {
            override fun getNmsSimpleNames(): List<String> {
                return listOf(/*"PacketPlayOutSetSlot", */"PacketPlayOutWindowItems", "ClientboundContainerSetContentPacket")
            }

            override fun checkCondition(message: Any): Boolean {
                return if (message::class.simpleName == "PacketPlayOutWindowItems" || message::class.simpleName == "ClientboundContainerSetContentPacket") {
                    val containerId = reflectionWrapper.getField(message::class, "containerId",
                        Version.V_17.handle("a"),
                        Version.V_17_FORGE.handle("f_131942_")
                    ).callAccess<Int>(message)
                    (containerId == targetContainer)
                } else false
            }

            override fun unregisterType(): HandlerUnregisterType {
                return HandlerUnregisterType.ALL
            }

            override fun unregisterCondition(message: Any): Boolean {
                return when (message::class.simpleName) {
                    "PacketPlayOutOpenWindow", "ClientboundOpenScreenPacket" -> {
                        val containerId = reflectionWrapper.getField(message::class, "containerId",
                            Version.V_17.handle("a"),
                            Version.V_17_FORGE.handle("f_132611_")
                        ).callAccess<Int>(message)
                        (containerId != targetContainer)
                    }
                    "PacketPlayOutCloseWindow", "ClientboundContainerClosePacket" -> {
                        val containerId = reflectionWrapper.getField(message::class, "containerId",
                            Version.V_17.handle("a"),
                            Version.V_17_FORGE.handle("f_131930_")
                        ).callAccess<Int>(message)
                        (containerId == targetContainer)
                    }
                    else -> false
                }
            }

            override fun handle(message: Any) {
                val clazz = message::class
                when (clazz.simpleName!!) {
                    "PacketPlayOutWindowItems", "ClientboundContainerSetContentPacket" -> {
                        val listField = reflectionWrapper.getField(message::class, "items",
                            Version.V_17.handle("c"),
                            Version.V_17_FORGE.handle("f_131943_")
                        )
                        val list = listField.callAccess<MutableList<Any>>(message)
                        list.forEachIndexed { index, any ->
                            val wrapper = itemStackService.getWrapper(any)
                            val bukkitItemStack = wrapper.getBukkitItemStack()
                            if (filter(index, bukkitItemStack)) {
                                list[index] = bukkitItemStack.apply { item(index, bukkitItemStack) }.getNmsItemStack()
                                    .getUnwrappedInstance()
                            }
                        }
                    }
                }
            }
        }
    }
}