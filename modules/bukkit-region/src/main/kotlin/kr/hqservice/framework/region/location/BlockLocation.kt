package kr.hqservice.framework.region.location

import kr.hqservice.framework.region.math.Point
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

interface BlockLocation {
    fun getWorld(): World

    fun getX(): Int

    fun getY(): Int

    fun getZ(): Int

    fun getPoint(): Point

    fun toLocation(): Location

    fun getBlock(): Block

    fun clone(): BlockLocation
}