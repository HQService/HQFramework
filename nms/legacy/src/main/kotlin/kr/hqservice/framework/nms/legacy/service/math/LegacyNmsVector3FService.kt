package kr.hqservice.framework.nms.legacy.service.math

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.math.LegacyVector3fWrapper
import kr.hqservice.framework.nms.service.math.NmsVector3fService
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import kotlin.reflect.KClass

class LegacyNmsVector3FService(
    private val reflectionWrapper: LegacyNmsReflectionWrapper
) : NmsVector3fService {
    private val vector3fClass = reflectionWrapper.getNmsClass("Vector3f",
        Version.V_17.handle("core")
    )
    private val vector3fConstructor =
        vector3fClass.java.getConstructor(Float::class.java, Float::class.java, Float::class.java)

    override fun wrap(target: Triple<Float, Float, Float>): Vector3fWrapper {
        val vector3f = vector3fConstructor.newInstance(target.first, target.second, target.third)
        return LegacyVector3fWrapper(vector3f, vector3fClass, reflectionWrapper)
    }

    override fun unwrap(wrapper: Vector3fWrapper): Triple<Float, Float, Float> {
        return Triple(wrapper.getX(), wrapper.getY(), wrapper.getZ())
    }

    override fun getOriginalClass(): KClass<*> {
        return Triple::class
    }

    override fun getTargetClass(): KClass<*> {
        return vector3fClass
    }
}