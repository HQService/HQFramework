package kr.hqservice.framework.region.location

import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World

data class RegionWorld(
    val worldName: String
) {

    constructor(world: World): this(world.name)

    fun getWorld(server: Server): World? {
        return server.getWorld(worldName)
    }

    override fun toString(): String {
        return worldName
    }
}

fun Location.toRegionWorld(): RegionWorld {
    return RegionWorld(world!!)
}