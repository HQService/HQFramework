package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.entity.VirtualSign
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import org.bukkit.entity.Player

class VirtualSignScope(
    player: Player,
    signFactory: VirtualEntityFactory
) {
    private var signPacket = VirtualSign(player, signFactory)
    private var confirmHandler: ((List<String>) -> Boolean)? = null

    fun setConfirmHandler(confirmHandler: (List<String>) -> Boolean) {
        this.confirmHandler = confirmHandler
    }

    internal fun confirm(texts: List<String>): Boolean {
        return confirmHandler?.invoke(texts) != false
    }

    fun getMessages(): Array<Virtual> {
        val messages = mutableListOf<Virtual>()
        signPacket.apply(messages::add)
        return messages.toTypedArray()
    }
}