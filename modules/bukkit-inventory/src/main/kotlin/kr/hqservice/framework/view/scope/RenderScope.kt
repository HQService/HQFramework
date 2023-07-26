package kr.hqservice.framework.view.scope

import kotlinx.coroutines.launch
import kr.hqservice.framework.nms.extension.virtual
import kr.hqservice.framework.view.InventoryLifecycle
import kr.hqservice.framework.view.element.TitleElement
import kr.hqservice.framework.view.state.SubscribableState
import org.bukkit.entity.Player

class RenderScope(
    inventoryLifecycle: InventoryLifecycle,
    private val player: Player
) : InventoryLifecycle by inventoryLifecycle {
    fun title(title: String, titleScope: TitleElement.() -> Unit = {}) {
        val titleElement = TitleElement(player, title)
        titleScope(titleElement)
        titleElement.subscribedStates.map { state ->
            launch {
                state as SubscribableState
                state.getStateFlow().collect {
                    val titleInvoked = titleElement.titleBuilder.invoke()
                    player.virtual {
                        inventory {
                            setTitle(titleInvoked)
                        }
                    }
                }
            }
        }
    }
}