package kr.hqservice.framework.nms.virtual.container

import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player

open class VirtualAnvilContainer(
    player: Player,
    title: BaseComponent
) : VirtualContainer(player, ComponentSerializer.toString(title))

class VirtualPaperAnvilContainer(
    player: Player,
    val adventure: Component
) : VirtualAnvilContainer(player, TextComponent())