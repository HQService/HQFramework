package kr.hqservice.framework.inventory.element

import kotlinx.coroutines.launch
import kr.hqservice.framework.inventory.coroutine.LifecycleOwner
import kr.hqservice.framework.inventory.state.State
import kr.hqservice.framework.nms.extension.virtual
import org.bukkit.entity.Player

class TitleElement(private val viewer: Player, private val title: String, private val lifecycleOwner: LifecycleOwner) : ViewElement {
    internal fun setTitle() {
        viewer.virtual {
            inventory {
                setTitle(title)
            }
        }
    }

    override fun subscribe(vararg states: State<*>) {
        states.forEach { state ->
            lifecycleOwner.launch {
                state.getStateFlow().collect {
                    setTitle()
                }
            }
        }
    }
}