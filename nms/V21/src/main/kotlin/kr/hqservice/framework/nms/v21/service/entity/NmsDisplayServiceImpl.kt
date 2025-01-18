package kr.hqservice.framework.nms.v21.service.entity

import kr.hqservice.framework.nms.service.entity.NmsDisplayService
import kr.hqservice.framework.nms.virtual.entity.AbstractVirtualDisplay
import kr.hqservice.framework.nms.wrapper.entity.NmsDisplayWrapper
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Display.BillboardConstraints
import net.minecraft.world.entity.Display.TextDisplay
import org.bukkit.Location
import kotlin.reflect.KClass

class NmsDisplayServiceImpl : NmsDisplayService {
    override fun initializeLocation(virtualDisplay: AbstractVirtualDisplay, location: Location) {
        val entity = virtualDisplay.getEntity() as TextDisplay
        entity.setPosRaw(location.x, location.y, location.z)
        entity.yRot = location.yaw
    }

    override fun applyTransformation(virtualDisplay: AbstractVirtualDisplay) {
        val entity = virtualDisplay.getEntity() as TextDisplay
        entity.setPosRaw(virtualDisplay.getLocation().x, virtualDisplay.getLocation().y, virtualDisplay.getLocation().z)
        entity.yRot = virtualDisplay.getLocation().yaw
    }

    override fun applyInterpolationDuration(virtualDisplay: AbstractVirtualDisplay) {
        val entity = virtualDisplay.getEntity() as TextDisplay
        entity.transformationInterpolationDuration = virtualDisplay.interpolationDuration
    }

    override fun applyInterpolationDelay(virtualDisplay: AbstractVirtualDisplay) {
        val entity = virtualDisplay.getEntity() as TextDisplay
        entity.transformationInterpolationDelay = virtualDisplay.interpolationDelay
    }

    override fun applyBillboard(virtualDisplay: AbstractVirtualDisplay) {
        val entity = virtualDisplay.getEntity() as TextDisplay
        entity.billboardConstraints = virtualDisplay.billboard.getUnwrappedInstance() as BillboardConstraints
    }

    override fun applyViewRange(virtualDisplay: AbstractVirtualDisplay) {
        val entity = virtualDisplay.getEntity() as TextDisplay
        entity.viewRange = virtualDisplay.viewDistance
    }

    override fun applyShadowRadius(virtualDisplay: AbstractVirtualDisplay) {
        val entity = virtualDisplay.getEntity() as TextDisplay
        entity.shadowRadius = virtualDisplay.shadowRadius
    }

    override fun wrap(target: Location): NmsDisplayWrapper {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun unwrap(wrapper: NmsDisplayWrapper): Location {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun getOriginalClass(): KClass<*> {
        return Location::class
    }

    override fun getTargetClass(): KClass<*> {
        return Display::class
    }
}