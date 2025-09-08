package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class VirtualContainer(
    val player: Player,
    val title: String
) : Virtual, KoinComponent {
    private val messageFactory: VirtualContainerMessageFactory by inject()

    override fun createVirtualMessage(): VirtualMessage? = messageFactory.create(this)
}

open class VirtualPaperContainer(
    player: Player,
    val adventure: Component
) : VirtualContainer(player, "paper-adventure")