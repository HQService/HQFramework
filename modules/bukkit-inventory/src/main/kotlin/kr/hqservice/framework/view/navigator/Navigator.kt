package kr.hqservice.framework.view.navigator

import kr.hqservice.framework.view.View
import org.bukkit.entity.Player
import java.util.*

interface Navigator {
    suspend fun goNext(view: View, vararg players: Player)

    suspend fun goPrevious(player: Player)

    suspend fun goFirst(player: Player)

    suspend fun clearViewsAndClose(player: Player)

    fun current(playerId: UUID): View?

    fun openedViews(playerId: UUID): List<View>
}