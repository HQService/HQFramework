package kr.hqservice.framework.view.navigator.impl

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.navigator.Navigator
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@HQSingleton
@Bean
internal class NavigatorImpl : Navigator {
    private val currentView: MutableMap<UUID, Stack<View>> = ConcurrentHashMap<UUID, Stack<View>>()
    private val changeViewAllows: MutableSet<UUID> = mutableSetOf()

    override suspend fun goNext(view: View, vararg playersInput: Player) {
        coroutineScope {
            val players = mutableListOf(*playersInput)
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
            view.open(*players.toTypedArray()) { player ->
                changeViewAllows.remove(player.uniqueId)
            }
        }
    }

    internal fun isAllow(playerId: UUID): Boolean {
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
            viewStack.peek().open(player) {
                changeViewAllows.remove(player.uniqueId)
            }
        }
    }

    override suspend fun goFirst(player: Player) {
        val currentView = currentView[player.uniqueId] ?: return
        if (currentView.size == 1) {
            changeViewAllows.add(player.uniqueId)
            currentView.pop().open(player) {
                changeViewAllows.remove(player.uniqueId)
            }
        } else if(currentView.size != 0) {
            while (currentView.size > 1) {
                currentView.pop()
            }
            changeViewAllows.add(player.uniqueId)
            currentView.first().open(player) {
                changeViewAllows.remove(player.uniqueId)
            }
        }
    }

    override suspend fun clearViews(player: Player) {
        currentView[player.uniqueId]?.clear()
    }

    override fun current(playerId: UUID): View? {
        return currentView[playerId]?.peek()
    }

    override fun openedViews(playerId: UUID): List<View> {
        return currentView[playerId] ?: emptyList()
    }
}