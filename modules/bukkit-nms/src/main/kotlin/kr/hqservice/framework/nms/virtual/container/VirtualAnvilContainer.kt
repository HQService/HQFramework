package kr.hqservice.framework.nms.virtual.container

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player

class VirtualAnvilContainer(
    player: Player,
    title: BaseComponent
) : VirtualContainer(player, ComponentSerializer.toString(title))