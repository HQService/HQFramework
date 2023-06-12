package kr.hqservice.framework.nms.wrapper

import org.bukkit.Location

interface EntityWrapper : NmsWrapper {
    fun getName(): String

    fun setName(name: String)

    fun isNameVisible(): Boolean

    fun getId(): Int

    fun getLocation(): Location

    fun teleport(location: Location)

    fun getNmsEntity(): Any

    fun isRemoved(): Boolean

    fun remove()
}