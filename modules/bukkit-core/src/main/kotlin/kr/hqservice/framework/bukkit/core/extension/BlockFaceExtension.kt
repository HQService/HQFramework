package kr.hqservice.framework.bukkit.core.extension

import org.bukkit.block.BlockFace

val BlockFace.yaw get() = when (this) {
    BlockFace.EAST -> -90f
    BlockFace.WEST -> 90f
    BlockFace.SOUTH -> 0f
    BlockFace.NORTH -> 180f
    else -> throw IllegalArgumentException()
}