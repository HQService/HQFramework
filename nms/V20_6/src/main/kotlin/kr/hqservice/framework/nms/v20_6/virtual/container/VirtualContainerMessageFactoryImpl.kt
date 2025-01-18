package kr.hqservice.framework.nms.v20_6.virtual.container

import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.container.VirtualAnvilContainer
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.v20_6.wrapper.container.ContainerWrapperImpl
import kr.hqservice.framework.nms.virtual.message.VirtualFunc
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.inventory.AnvilMenu
import net.minecraft.world.inventory.MenuType
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryView

class VirtualContainerMessageFactoryImpl(
    private val baseComponentService: NmsBaseComponentService,
    private val containerService: NmsContainerService
) : VirtualContainerMessageFactory {
    companion object {
        private val genericContainers = listOf(
            MenuType.GENERIC_3x3,
            MenuType.GENERIC_9x1,
            MenuType.GENERIC_9x2,
            MenuType.GENERIC_9x3,
            MenuType.GENERIC_9x4,
            MenuType.GENERIC_9x5,
            MenuType.GENERIC_9x6
        )
    }

    override fun create(virtualContainer: VirtualContainer): VirtualMessage? {
        if (virtualContainer is VirtualAnvilContainer)
            return createAnvil(virtualContainer)

        return createDefault(virtualContainer)
    }

    private fun createDefault(virtualContainer: VirtualContainer): VirtualMessage?{
        val container = (containerService.wrap(virtualContainer.player) as ContainerWrapperImpl).getUnwrappedInstance()

        val bukkitView = container.bukkitView
        if (bukkitView !is InventoryView) throw UnsupportedOperationException("error1")

        val type = bukkitView.topInventory.type
        val size = bukkitView.topInventory.size
        val virtualContainerType = if (type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST) {
            genericContainers[size / 9]
        } else {
            try {
                container.type
            } catch (_: Exception) {
                return null
            }
        }

        return VirtualMessageImpl(
            ClientboundOpenScreenPacket(
                container.containerId,
                virtualContainerType,
                baseComponentService.wrap(virtualContainer.title).getUnwrappedInstance() as Component
            )
        )
    }

    private fun createAnvil(virtualContainer: VirtualAnvilContainer): VirtualMessage {
        val container = containerService.wrap(virtualContainer.player)

        return VirtualMessageImpl(ClientboundOpenScreenPacket(
            container.getContainerId(),
            MenuType.ANVIL,
            baseComponentService.wrap(virtualContainer.title).getUnwrappedInstance() as Component
        ))
    }
}