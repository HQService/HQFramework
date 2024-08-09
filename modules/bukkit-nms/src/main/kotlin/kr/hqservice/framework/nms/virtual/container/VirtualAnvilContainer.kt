package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.inventory.MenuType
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

class VirtualAnvilContainer(
    private val player: Player,
    private val title: BaseComponent
) : VirtualContainer(player, ComponentSerializer.toString(title)) {
    override fun createVirtualMessage(): VirtualMessage {
        val container = (player as CraftPlayer).handle.containerMenu

        val virtualContainerType = MenuType.ANVIL

        return VirtualMessageImpl(
            ClientboundOpenScreenPacket(
                container.containerId,
                virtualContainerType,
                baseComponentService.wrap(ComponentSerializer.toString(title)).getUnwrappedInstance() as Component
            )
        )
    }
}