package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.inventory.MenuType
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

open class VirtualContainer(
    private val player: Player,
    private val title: String
) : Virtual, KoinComponent {
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

    protected val baseComponentService: NmsService<String, BaseComponentWrapper> by inject(named("base-component"))
    override fun createVirtualMessage(): VirtualMessage? {
        val container = (player as CraftPlayer).handle.containerMenu

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
                baseComponentService.wrap(title).getUnwrappedInstance() as Component
            )
        )
    }
}