package kr.hqservice.framework.view.navigator

import kr.hqservice.framework.view.HQView
import org.bukkit.entity.Player
import java.util.UUID

interface Navigator {
    suspend fun goNext(view: HQView, vararg players: Player)

    suspend fun goPrevious(player: Player)

    /**
     * 제일 첫번째 view 로 이동시킵니다.
     *
     *
     */
    suspend fun goFirst(player: Player)

    fun current(player: Player): HQView?

    fun current(playerId: UUID): HQView?

    fun openedViews(player: Player): List<HQView>

    fun openedViews(playerId: UUID): List<HQView>
}