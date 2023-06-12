package kr.hqservice.framework.nms.wrapper.entity

import kr.hqservice.framework.nms.wrapper.EntityWrapper
import org.bukkit.Location

class NmsArmorStandWrapper(
    private val id: Int,
    private val baseEntity: Any,
    private var location: Location,
    private var name: String,
) : EntityWrapper {
    override fun getName(): String {
        return name
    }

    override fun setName(name: String) {
        TODO("Not yet implemented")
    }

    override fun isNameVisible(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getId(): Int {
        return id
    }

    override fun getLocation(): Location {
        TODO("Not yet implemented")
    }

    override fun teleport(location: Location) {
        TODO("Not yet implemented")
    }

    override fun getNmsEntity(): Any {
        return baseEntity
    }

    override fun getUnwrappedInstance(): Any {
        return baseEntity
    }
}