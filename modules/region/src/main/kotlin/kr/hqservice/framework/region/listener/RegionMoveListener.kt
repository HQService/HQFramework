package kr.hqservice.framework.region.listener

import kr.hqservice.framework.region.event.impl.RegionEnterEvent
import kr.hqservice.framework.region.event.impl.RegionLeaveEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

class RegionMoveListener : Listener {

    private enum class MoveType {
        MOVE, TELEPORT, RESPAWN, JOIN, QUIT, DEATH
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        checker(event.player, event.to, MoveType.MOVE, event.from)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        checker(event.player, event.player.location, MoveType.JOIN, null)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        checker(event.player, null, MoveType.QUIT, event.player.location)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        checker(event.player, event.respawnLocation, MoveType.RESPAWN, null)
    }

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        checker(event.player, event.to, MoveType.TELEPORT, event.from)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        checker(event.entity, null, MoveType.DEATH, event.entity.location)
    }

    private fun checker(player: Player, to: Location?, type: MoveType, from: Location?) {
        val toRegion = to?.run(cupboardRepository::inRange)
        val fromRegion = from?.run(cupboardRepository::inRange)

        if (fromRegion != null) {
            val leaveEvent = RegionLeaveEvent(player, CupboardRegionLeaveEvent.MoveType.valueOf(type.name), fromRegion, toRegion)
            plugin.server.pluginManager.callEvent(leaveEvent)
        }
        if (toRegion != null) {
            val enterEvent = RegionEnterEvent(player, CupboardRegionEnterEvent.MoveType.valueOf(type.name), fromRegion, toRegion)
            plugin.server.pluginManager.callEvent(enterEvent)
        }
    }
}