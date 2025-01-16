package kr.hqservice.framework.nms.virtual.entity

import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.koin.core.component.inject

class VirtualTextDisplay(
    location: Location,
    private val init: VirtualTextDisplay.() -> Unit = {}
) : AbstractVirtualDisplay(location, "") {
    private val service: NmsTextDisplayService by inject()
    private var wrapper = service.wrap(location)

    var backgroundColor: Color = Color.WHITE
        set(value) {
            field = value
            wrapper.setBackgroundColor(value)
            switchMetaMask()
        }

    // 1.0 ~ 0
    var opacity: Float = 1.0f
        set(value) {
            field = value
            wrapper.setOpacity(value)
            switchMetaMask()
        }

    var text: BaseComponent = TextComponent("")
        set(value) {
            field = value
            wrapper.setText(value)
            switchMetaMask()
        }

    override fun getEntity(): Any {
        return wrapper.getUnwrappedInstance()
    }

    override fun initialize() {
        service.setLineWith(wrapper, 1000)
        super.initialize()
        init(this)
    }
}