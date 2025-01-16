package kr.hqservice.framework.nms.legacy.virtual.entity

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboard
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboardFactory

class LegacyHQBillboardFactory(
    reflectionWrapper: LegacyNmsReflectionWrapper
) : HQBillboardFactory {
    private val billboardClass = reflectionWrapper.getNmsClass("Display\$BillboardConstraints",
        Version.V_19.handle("world.entity")
    )
    private val valueOfFunc = reflectionWrapper.getFunction(billboardClass,
        FunctionType("valueOf", null, listOf(String::class), true)
    )
    private fun nmsValueOf(nmsName: String): Any {
        return valueOfFunc.call(nmsName)!!
    }

    override fun create(nmsName: String): HQBillboard {
        return LegacyHQBillboard(nmsValueOf(nmsName))
    }
}