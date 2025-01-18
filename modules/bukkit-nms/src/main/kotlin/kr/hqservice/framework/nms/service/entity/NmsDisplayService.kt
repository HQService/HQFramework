package kr.hqservice.framework.nms.service.entity

import kr.hqservice.framework.nms.service.NmsEntityService
import kr.hqservice.framework.nms.virtual.entity.AbstractVirtualDisplay
import kr.hqservice.framework.nms.wrapper.entity.NmsDisplayWrapper
import org.bukkit.Location

interface NmsDisplayService : NmsEntityService<NmsDisplayWrapper> {
    fun initializeLocation(virtualDisplay: AbstractVirtualDisplay, location: Location)

    fun applyTransformation(virtualDisplay: AbstractVirtualDisplay)

    fun applyInterpolationDuration(virtualDisplay: AbstractVirtualDisplay)

    fun applyInterpolationDelay(virtualDisplay: AbstractVirtualDisplay)

    fun applyBillboard(virtualDisplay: AbstractVirtualDisplay)

    fun applyViewRange(virtualDisplay: AbstractVirtualDisplay)

    fun applyShadowRadius(virtualDisplay: AbstractVirtualDisplay)
}