package kr.hqservice.framework.nms.v21.virtual.entity

import kr.hqservice.framework.nms.virtual.entity.display.HQBillboard
import net.minecraft.world.entity.Display.BillboardConstraints

class HQBillboardImpl(
    private val billboardConstraints: BillboardConstraints
) : HQBillboard {
    override fun getUnwrappedInstance(): BillboardConstraints {
        return billboardConstraints
    }
}