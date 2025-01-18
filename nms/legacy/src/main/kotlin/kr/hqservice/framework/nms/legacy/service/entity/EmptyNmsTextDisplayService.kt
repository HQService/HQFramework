package kr.hqservice.framework.nms.legacy.service.entity

import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import kr.hqservice.framework.nms.wrapper.entity.NmsTextDisplayWrapper
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Color
import org.bukkit.Location
import kotlin.reflect.KClass

class EmptyNmsTextDisplayService : NmsTextDisplayService {
    override fun setText(displayWrapper: NmsTextDisplayWrapper, text: BaseComponent) {
        TODO("Not yet implemented")
    }

    override fun setBackgroundColor(displayWrapper: NmsTextDisplayWrapper, color: Color) {
        TODO("Not yet implemented")
    }

    override fun setLineWith(displayWrapper: NmsTextDisplayWrapper, lineWidth: Int) {
        TODO("Not yet implemented")
    }

    override fun setOpacity(displayWrapper: NmsTextDisplayWrapper, opacity: Byte) {
        TODO("Not yet implemented")
    }

    override fun wrap(target: Location): NmsTextDisplayWrapper {
        TODO("Not yet implemented")
    }

    override fun unwrap(wrapper: NmsTextDisplayWrapper): Location {
        TODO("Not yet implemented")
    }

    override fun getOriginalClass(): KClass<*> {
        TODO("Not yet implemented")
    }

    override fun getTargetClass(): KClass<*> {
        TODO("Not yet implemented")
    }
}