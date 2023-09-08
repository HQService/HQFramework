package kr.hqservice.framework.view.navigator.impl

import io.netty.util.internal.ConcurrentSet
import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.navigator.Navigator
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@Bean
internal class NavigatorImpl : Navigator {
    private val currentView: MutableMap<UUID, Stack<View>> = ConcurrentHashMap<UUID, Stack<View>>()
    private val changeViewAllows: MutableSet<UUID> = ConcurrentSet()

    override suspend fun goNext(view: View, vararg playersInput: Player) {
        coroutineScope {
            playersInput.mapNotNull { player ->
                if (changeViewAllows.contains(player.uniqueId)) {
                    return@mapNotNull null
                }
                launch {
                    if (player.openInventory.topInventory.holder is View) {
                        changeViewAllows.add(player.uniqueId)
                        withContext(Dispatchers.BukkitMain) {
                            player.closeInventory()
                        }
                    }
                    currentView.computeIfAbsent(player.uniqueId) { Stack() }.push(view)
                }
            }.joinAll()
            nonSuspendOpen(coroutineContext, view, *playersInput)
        }
    }

    private fun nonSuspendOpen(coroutineContext: CoroutineContext, view: View, vararg players: Player) {
        CoroutineScope(coroutineContext).launch {
            view.open(*players) { player ->
                changeViewAllows.remove(player.uniqueId)
            }
        }
    }

    internal fun isAllowToChangeView(playerId: UUID): Boolean {
        return changeViewAllows.contains(playerId)
    }

    override suspend fun goPrevious(player: Player) {
        val viewStack = currentView[player.uniqueId]
        if (viewStack == null) {
            player.closeInventory()
            return
        }
        viewStack.pop()
        if (viewStack.isNotEmpty()) {
            changeViewAllows.add(player.uniqueId)
            nonSuspendOpen(coroutineContext, viewStack.peek(), player)
        }
    }

    override suspend fun goFirst(player: Player) {
        val currentView = currentView[player.uniqueId] ?: return
        if (currentView.size == 1) {
            changeViewAllows.add(player.uniqueId)
            nonSuspendOpen(coroutineContext, currentView.pop(), player)
        } else if(currentView.size != 0) {
            while (currentView.size > 1) {
                currentView.pop()
            }
            changeViewAllows.add(player.uniqueId)
            nonSuspendOpen(coroutineContext, currentView.first(), player)
        }
    }

    override suspend fun clearViewsAndClose(player: Player) {
        currentView[player.uniqueId]?.clear()
        changeViewAllows.add(player.uniqueId)
        withContext(Dispatchers.BukkitMain) {
            player.closeInventory()
            changeViewAllows.remove(player.uniqueId)
        }
    }

    override fun current(playerId: UUID): View? {
        return currentView[playerId]?.peek()
    }

    override fun openedViews(playerId: UUID): List<View> {
        return currentView[playerId] ?: emptyList()
    }
}