package kr.hqservice.framework.nms.v20_6.virtual.entity

import kr.hqservice.framework.nms.virtual.entity.display.HQBillboard
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboardFactory
import net.minecraft.world.entity.Display.BillboardConstraints

class HQBillboardFactoryImpl : HQBillboardFactory {
    override fun create(nmsName: String): HQBillboard {
        return HQBillboardImpl(BillboardConstraints.valueOf(nmsName))
    }
}