package kr.hqservice.framework.view.navigator.impl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.view.HQView
import kr.hqservice.framework.view.navigator.Navigator
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@HQSingleton
@Bean
class NavigatorImpl : Navigator {
    private val currentView: MutableMap<UUID, Stack<HQView>> = ConcurrentHashMap<UUID, Stack<HQView>>()

    override suspend fun goNext(view: HQView, vararg players: Player) = coroutineScope {
        players.map { player ->
            launch {
                if (player.openInventory.topInventory.holder is HQView) {
                    player.closeInventory()
                    bukkitDelay(1)
                }
                currentView.computeIfAbsent(player.uniqueId) { Stack() }.push(view)
            }
        }.joinAll()

        view.open(*players)
    }

    override suspend fun goPrevious(player: Player) {
        val viewStack = currentView[player.uniqueId]
        if (viewStack == null) {
            player.closeInventory()
            return
        }
        viewStack.pop()
        if (viewStack.isNotEmpty()) {
            viewStack.peek().open(player)
        }
    }

    override suspend fun goFirst(player: Player) {
        val currentView = currentView[player.uniqueId] ?: return
        if (currentView.size == 1) {
            currentView.pop().open(player)
        } else {
            while (currentView.size > 1) {
                currentView.pop()
            }
        }
    }

    override fun current(player: Player): HQView? {
        return currentView[player.uniqueId]?.peek()
    }

    override fun current(playerId: UUID): HQView? {
        return currentView[playerId]?.peek()
    }

    override fun openedViews(player: Player): List<HQView> {
        return currentView[player.uniqueId] ?: emptyList()
    }

    override fun openedViews(playerId: UUID): List<HQView> {
        return currentView[playerId] ?: emptyList()
    }
}