package kr.hqservice.framework.view.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.hqservice.framework.nms.extension.virtual
import kr.hqservice.framework.view.element.TitleElement
import kr.hqservice.framework.view.state.SubscribableState
import org.bukkit.entity.Player

class RenderScope(
    coroutineScope: CoroutineScope,
    private val player: Player
) : CoroutineScope by coroutineScope {
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