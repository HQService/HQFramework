package kr.hqservice.framework.nms.legacy.service.entity

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.service.entity.NmsDisplayService
import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.wrapper.entity.NmsTextDisplayWrapper
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Color
import org.bukkit.Location
import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible

class LegacyNmsTextDisplayService(
    private val worldService: NmsWorldService,
    private val nmsDisplayService: NmsDisplayService,
    private val baseComponentService: NmsBaseComponentService,
    reflectionWrapper: LegacyNmsReflectionWrapper
) : NmsTextDisplayService {
    private val setTextFunction = reflectionWrapper.getFunction(getTargetClass(), "setText", listOf(baseComponentService.getTargetClass()),
        Version.V_19.handleFunction("c") { setParameterClasses(baseComponentService.getTargetClass()) },
        Version.V_19_FORGE.handleFunction("m_269037_") { setParameterClasses(baseComponentService.getTargetClass()) }
    )
    private val setBackgroundColorFunction = reflectionWrapper.getFunction(getTargetClass(), "setBackgroundColor", listOf(Int::class),
        Version.V_19.handleFunction("c") { setParameterClasses(Int::class) },
        Version.V_19_FORGE.handleFunction("m_269001_") { setParameterClasses(Int::class) }
    )
    private val DATA_LINE_WIDTH_ID = reflectionWrapper.getStaticField(getTargetClass(), "DATA_LINE_WIDTH_ID",
        Version.V_19.handle("aL"),
        Version.V_20.handle("aM"),
        Version.V_20_2.handle("aN"),
        Version.V_19_FORGE.handle("f_268476_")
    ).run {
        isAccessible = true
        call()!!
    }
    private val DATA_TEXT_OPACITY_ID = reflectionWrapper.getStaticField(getTargetClass(), "DATA_TEXT_OPACITY_ID",
        Version.V_19.handle("aN"),
        Version.V_20.handle("aO"),
        Version.V_20_2.handle("aP"),
        Version.V_19_FORGE.handle("f_268481_")
    ).run {
        isAccessible = true
        call()!!
    }


    override fun setText(displayWrapper: NmsTextDisplayWrapper, text: BaseComponent) {
        val baseCompWrapper = baseComponentService.wrapFromJson(ComponentSerializer.toString(text))
        if (!setBackgroundColorFunction.isAccessible)
            setBackgroundColorFunction.isAccessible = true
        setTextFunction.call(displayWrapper.getUnwrappedInstance(), baseCompWrapper.getUnwrappedInstance())
    }

    override fun setBackgroundColor(displayWrapper: NmsTextDisplayWrapper, color: Color) {
        if (!setBackgroundColorFunction.isAccessible)
            setBackgroundColorFunction.isAccessible = true
        setBackgroundColorFunction.call(displayWrapper.getUnwrappedInstance(), color.asARGB())
    }

    override fun setLineWith(displayWrapper: NmsTextDisplayWrapper, lineWidth: Int) {
        nmsDisplayService as LegacyNmsDisplayService
        nmsDisplayService.setEntityData(displayWrapper, DATA_LINE_WIDTH_ID, lineWidth)
    }

    override fun setOpacity(displayWrapper: NmsTextDisplayWrapper, opacity: Byte) {
        nmsDisplayService as LegacyNmsDisplayService
        nmsDisplayService.setEntityData(displayWrapper, DATA_TEXT_OPACITY_ID, opacity)
    }

    override fun wrap(target: Location): NmsTextDisplayWrapper {
        nmsDisplayService as LegacyNmsDisplayService
        val const = nmsDisplayService.getConstructor(nmsDisplayService.textDisplayType::class, "TextDisplay")
        val level = worldService.wrap(target.world!!).getUnwrappedInstance()
        return NmsTextDisplayWrapper(const.newInstance(nmsDisplayService.textDisplayType, level), this)
    }

    override fun unwrap(wrapper: NmsTextDisplayWrapper): Location {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun getOriginalClass(): KClass<*> {
        return Location::class
    }

    override fun getTargetClass(): KClass<*> {
        nmsDisplayService as LegacyNmsDisplayService
        return nmsDisplayService.getDisplayClass("TextDisplay")
    }
}