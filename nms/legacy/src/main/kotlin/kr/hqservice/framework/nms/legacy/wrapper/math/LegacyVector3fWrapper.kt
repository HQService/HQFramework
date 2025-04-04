package kr.hqservice.framework.nms.legacy.wrapper.math

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import kotlin.reflect.KClass

class LegacyVector3fWrapper(
    private val vector3f: Any,
    targetClass: KClass<*>,
    reflectionWrapper: LegacyNmsReflectionWrapper
) : Vector3fWrapper() {
    private val getXFunction = reflectionWrapper.getFunction(targetClass, "getX",
        Version.V_18.handleFunction("b"),
        Version.V_17_FORGE.handleFunction("m_123156_")
    )

    private val getYFunction = reflectionWrapper.getFunction(targetClass, "getY",
        Version.V_18.handleFunction("c"),
        Version.V_17_FORGE.handleFunction("m_123157_")
    )

    private val getZFunction = reflectionWrapper.getFunction(targetClass, "getZ",
        Version.V_18.handleFunction("d"),
        Version.V_17_FORGE.handleFunction("m_123158_")
    )

    override fun getX(): Float = getXFunction.call(vector3f) as Float
    override fun getY(): Float = getYFunction.call(vector3f) as Float
    override fun getZ(): Float = getZFunction.call(vector3f) as Float

    override fun getUnwrappedInstance(): Any {
        return vector3f
    }
}