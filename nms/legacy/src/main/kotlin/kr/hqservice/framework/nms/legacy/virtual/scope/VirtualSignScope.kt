package kr.hqservice.framework.nms.legacy.virtual.scope

import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.entity.inner.VirtualSign
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

class VirtualSignScope(
    player: Player,
    reflectionWrapper: NmsReflectionWrapper
) : KoinComponent {

    private var signPacket = VirtualSign(player, reflectionWrapper)

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