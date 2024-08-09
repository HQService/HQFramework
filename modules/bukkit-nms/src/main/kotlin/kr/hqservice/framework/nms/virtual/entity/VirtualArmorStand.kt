package kr.hqservice.framework.nms.virtual.entity

import kr.hqservice.framework.nms.service.entity.NmsArmorStandService
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Location
import org.koin.core.component.inject

class VirtualArmorStand(
    location: Location,
    name: String = "",
    private val init: VirtualArmorStand.() -> Unit = {}
) : AbstractVirtualEntity(location, name) {
    private val service: NmsArmorStandService by inject()
    private var wrapper: NmsArmorStandWrapper = service.wrap(location)

    override fun getEntity(): Entity {
        return wrapper.getUnwrappedInstance()
    }

    override fun initialize() {
        init(this)
    }

    fun setSmall(small: Boolean) {
        wrapper.setSmall(small)
        switchMetaMask()
    }

    fun getHeadPose(): Triple<Float, Float, Float> {
        return wrapper.getHeadPose()
    }

    fun setHeadPose(x: Float, y: Float, z: Float) {
        wrapper.setHeadPose(Triple(x, y, z))
        switchMetaMask()
    }

    fun setMarker(marker: Boolean) {
        wrapper.setMarker(marker)
        switchMetaMask()
    }

    fun setArms(arms: Boolean) {
        wrapper.setArms(arms)
        switchMetaMask()
    }

    fun setBasePlate(basePlate: Boolean) {
        wrapper.setBasePlate(basePlate)
        switchMetaMask()
    }
}

