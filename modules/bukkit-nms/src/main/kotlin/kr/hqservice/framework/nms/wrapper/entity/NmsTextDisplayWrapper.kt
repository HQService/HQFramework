package kr.hqservice.framework.nms.wrapper.entity

import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Color

class NmsTextDisplayWrapper(
    nmsInstance: Any,
    private val service: NmsTextDisplayService
) : NmsDisplayWrapper(nmsInstance) {
    fun setText(text: BaseComponent) {
        service.setText(this, text)
    }

    fun setOpacity(opacity: Float) {
        val fixedValue = (opacity * 255).run {
            if (this < 0) 0
            else (this % 256).toInt()
        }
        service.setOpacity(this, fixedValue.toByte())
    }

    fun setBackgroundColor(color: Color) {
        service.setBackgroundColor(this, color)
    }
}