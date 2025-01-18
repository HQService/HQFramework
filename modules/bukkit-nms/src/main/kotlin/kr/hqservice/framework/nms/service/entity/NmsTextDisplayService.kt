package kr.hqservice.framework.nms.service.entity

import kr.hqservice.framework.nms.service.NmsEntityService
import kr.hqservice.framework.nms.wrapper.entity.NmsTextDisplayWrapper
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Color

interface NmsTextDisplayService : NmsEntityService<NmsTextDisplayWrapper> {
    fun setText(displayWrapper: NmsTextDisplayWrapper, text: BaseComponent)

    fun setBackgroundColor(displayWrapper: NmsTextDisplayWrapper, color: Color)

    fun setLineWith(displayWrapper: NmsTextDisplayWrapper, lineWidth: Int)

    fun setOpacity(displayWrapper: NmsTextDisplayWrapper, opacity: Byte)
}