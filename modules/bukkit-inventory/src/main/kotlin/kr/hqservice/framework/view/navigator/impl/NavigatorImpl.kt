package kr.hqservice.framework.view.navigator.impl

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.util.PluginScopeFinder
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.view.View
import kr.hqservice.framework.view.navigator.Navigator
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Bean
internal class NavigatorImpl(
    private val coroutineScope: CoroutineScope
) : Navigator {
    private val currentView: MutableMap<UUID, Stack<View>> = ConcurrentHashMap<UUID, Stack<View>>()
    private val changeViewAllows: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    override suspend fun goNext(view: View, vararg playersInput: Player) {
        val players = playersInput.filterNot { player -> changeViewAllows.contains(player.uniqueId) }
        coroutineScope {
            players.map { player ->
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
        }
        if (players.isNotEmpty()) nonSuspendOpen(view, players)
    }

    private fun nonSuspendOpen(view: View, players: List<Player>) {
        val pendingPlayerIds: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
        players.mapTo(pendingPlayerIds) { player -> player.uniqueId }
        val scope = PluginScopeFinder.find(view::class) ?: coroutineScope
        scope.launch {
            try {
                view.open(*players.toTypedArray()) { player ->
                    pendingPlayerIds.remove(player.uniqueId)
                    changeViewAllows.remove(player.uniqueId)
                }
            } finally {
                changeViewAllows.removeAll(pendingPlayerIds)
            }
        }
    }

    internal fun isAllowToChangeView(playerId: UUID): Boolean {
        return changeViewAllows.contains(playerId)
    }

    override suspend fun goPrevious(player: Player) {
        val viewStack = currentView[player.uniqueId]
        if (viewStack == null || viewStack.isEmpty()) {
            withContext(Dispatchers.BukkitMain) {
                player.closeInventory()
            }
            return
        }

        val poppedView = viewStack.pop()
        disposeIfUnused(poppedView)

        if (viewStack.isNotEmpty()) {
            changeViewAllows.add(player.uniqueId)
            nonSuspendOpen(viewStack.peek(), listOf(player))
        }
    }

    override suspend fun goFirst(player: Player) {
        val currentView = currentView[player.uniqueId] ?: return
        if (currentView.isEmpty()) return
        val poppedViews = mutableListOf<View>()
        while (currentView.size > 1) {
            poppedViews.add(currentView.pop())
        }
        poppedViews.forEach(::disposeIfUnused)
        changeViewAllows.add(player.uniqueId)
        nonSuspendOpen(currentView.first(), listOf(player))
    }

    override suspend fun clearViewsAndClose(player: Player) {
        val poppedViews = currentView[player.uniqueId]?.popAll() ?: emptyList()
        changeViewAllows.add(player.uniqueId)
        withContext(Dispatchers.BukkitMain) {
            player.closeInventory()
            changeViewAllows.remove(player.uniqueId)
        }
        poppedViews.forEach(::disposeIfUnused)
    }

    internal fun clear(player: Player) {
        changeViewAllows.remove(player.uniqueId)
        val poppedViews = currentView.remove(player.uniqueId)?.popAll() ?: return
        val openedHolder = player.openInventory.topInventory.holder
        poppedViews.firstOrNull { view -> view === openedHolder }?.invokeOnClose(player)
        poppedViews.forEach(::disposeIfUnused)
    }

    override fun current(playerId: UUID): View? {
        val viewStack = currentView[playerId] ?: return null
        return synchronized(viewStack) {
            if (viewStack.isEmpty()) null else viewStack.peek()
        }
    }

    override fun openedViews(playerId: UUID): List<View> {
        return currentView[playerId] ?: emptyList()
    }

    private fun Stack<View>.popAll(): List<View> {
        return synchronized(this) {
            val poppedViews = asReversed().toList()
            clear()
            poppedViews
        }
    }

    private fun disposeIfUnused(view: View) {
        val isStillOpened = view.viewerIds.any { viewerId ->
            currentView[viewerId]?.let { viewStack -> synchronized(viewStack) { viewStack.any { it === view } } } == true
        }
        if (!isStillOpened) view.dispose()
    }
}
