package kr.hqservice.framework.nms.v21.service.entity

import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import kr.hqservice.framework.nms.wrapper.entity.NmsTextDisplayWrapper
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import kotlin.reflect.KClass

class NmsTextDisplayServiceImpl(
    private val baseComponentService: NmsBaseComponentService
) : NmsTextDisplayService {
    override fun setText(displayWrapper: NmsTextDisplayWrapper, text: BaseComponent) {
        val baseCompWrapper = baseComponentService.wrapFromJson(ComponentSerializer.toString(text))
        val textDisplay = displayWrapper.getUnwrappedInstance() as TextDisplay
        textDisplay.text = baseCompWrapper.getUnwrappedInstance() as Component
    }

    override fun setBackgroundColor(displayWrapper: NmsTextDisplayWrapper, color: Color) {
        val textDisplay = displayWrapper.getUnwrappedInstance() as TextDisplay
        textDisplay.entityData.set(TextDisplay.DATA_BACKGROUND_COLOR_ID, color.asARGB())
    }

    override fun setLineWith(displayWrapper: NmsTextDisplayWrapper, lineWidth: Int) {
        val textDisplay = displayWrapper.getUnwrappedInstance() as TextDisplay
        textDisplay.entityData.set(TextDisplay.DATA_LINE_WIDTH_ID, lineWidth)
    }

    override fun setOpacity(displayWrapper: NmsTextDisplayWrapper, opacity: Byte) {
        val textDisplay = displayWrapper.getUnwrappedInstance() as TextDisplay
        textDisplay.textOpacity = opacity
    }

    override fun wrap(target: Location): NmsTextDisplayWrapper {
        val level = (target.world as CraftWorld).handle
        return NmsTextDisplayWrapper(TextDisplay(EntityType.TEXT_DISPLAY, level), this)
    }

    override fun unwrap(wrapper: NmsTextDisplayWrapper): Location {
        throw UnsupportedOperationException("")
    }

    override fun getOriginalClass(): KClass<*> {
        return Location::class
    }

    override fun getTargetClass(): KClass<*> {
        return TextDisplay::class
    }
}