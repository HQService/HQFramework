package kr.hqservice.framework.view.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.hqservice.framework.nms.extension.virtual
import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.element.TitleElement
import kr.hqservice.framework.view.state.SubscribableState
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

class RenderScope(
    private val view: View,
    coroutineScope: CoroutineScope,
    private val player: Player
) : CoroutineScope by coroutineScope {
    fun title(titleScope: TitleElement.() -> Unit = {}) {
        val titleElement = TitleElement()
        titleScope(titleElement)
        titleElement.subscribedStates.map { state ->
            val subscribe = launch {
                state as SubscribableState
                state.getStateFlow().collect {
                    val titleInvoked = titleElement.titleBuilder.invoke()
                    player.virtual {
                        inventory {
                            setTitle(titleInvoked ?: TextComponent(""))
                        }
                    }
                }
            }
            view.subscribes.add(subscribe)
        }
    }
}