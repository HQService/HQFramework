package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
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