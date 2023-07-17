package kr.hqservice.framework.view.navigator

import kr.hqservice.framework.view.HQView
import org.bukkit.entity.Player

class NavigatorContext(
    val player: Player,
    private val navigator: Navigator
) {
    suspend fun goPrevious() {
        navigator.goPrevious(player)
    }

    fun getOpenedViews(): List<HQView> {
        return navigator.openedViews(player.uniqueId)
    }
}