package kr.hqservice.framework.nms.legacy.service.entity

import kr.hqservice.framework.nms.service.entity.NmsDisplayService
import kr.hqservice.framework.nms.virtual.entity.AbstractVirtualDisplay
import kr.hqservice.framework.nms.wrapper.entity.NmsDisplayWrapper
import org.bukkit.Location
import kotlin.reflect.KClass

class EmptyNmsDisplayService : NmsDisplayService {
    override fun initializeLocation(virtualDisplay: AbstractVirtualDisplay, location: Location) {
        TODO("Not yet implemented")
    }

    override fun applyTransformation(virtualDisplay: AbstractVirtualDisplay) {
        TODO("Not yet implemented")
    }

    override fun applyInterpolationDuration(virtualDisplay: AbstractVirtualDisplay) {
        TODO("Not yet implemented")
    }

    override fun applyInterpolationDelay(virtualDisplay: AbstractVirtualDisplay) {
        TODO("Not yet implemented")
    }

    override fun applyBillboard(virtualDisplay: AbstractVirtualDisplay) {
        TODO("Not yet implemented")
    }

    override fun applyViewRange(virtualDisplay: AbstractVirtualDisplay) {
        TODO("Not yet implemented")
    }

    override fun applyShadowRadius(virtualDisplay: AbstractVirtualDisplay) {
        TODO("Not yet implemented")
    }

    override fun wrap(target: Location): NmsDisplayWrapper {
        TODO("Not yet implemented")
    }

    override fun unwrap(wrapper: NmsDisplayWrapper): Location {
        TODO("Not yet implemented")
    }

    override fun getOriginalClass(): KClass<*> {
        TODO("Not yet implemented")
    }

    override fun getTargetClass(): KClass<*> {
        TODO("Not yet implemented")
    }
}