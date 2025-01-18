package kr.hqservice.framework.nms.v20_6.virtual.entity

import kr.hqservice.framework.nms.virtual.entity.display.HQBillboard
import net.minecraft.world.entity.Display.BillboardConstraints

class HQBillboardImpl(
    private val billboardConstraints: BillboardConstraints
) : HQBillboard {
    override fun getUnwrappedInstance(): BillboardConstraints {
        return billboardConstraints
    }
}