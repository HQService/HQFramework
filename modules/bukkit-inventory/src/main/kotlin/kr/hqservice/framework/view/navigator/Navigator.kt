package kr.hqservice.framework.view.navigator

import kr.hqservice.framework.view.View
import org.bukkit.entity.Player
import java.util.UUID

interface Navigator {
    suspend fun goNext(view: View, vararg players: Player)

    suspend fun goPrevious(player: Player)

    /**
     * 제일 첫번째 view 로 이동시킵니다.
     *
     *
     */
    suspend fun goFirst(player: Player)

    suspend fun clearViews(player: Player)

    fun current(playerId: UUID): View?

    fun openedViews(playerId: UUID): List<View>
}