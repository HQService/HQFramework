package kr.hqservice.framework.nms.service.math

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import org.koin.core.annotation.Named
import kotlin.reflect.KClass

@Component
@Named("vector3f")
@HQSingleton(binds = [NmsService::class])
class Vector3fService(
    private val reflectionUtil: NmsReflectionWrapper
) : NmsService<Triple<Float, Float, Float>, Vector3fWrapper> {
    private val vector3fClass = reflectionUtil.getNmsClass("Vector3f",
        Version.V_15.handle("core"))
    private val vector3fConstructor = vector3fClass.java.getConstructor(Float::class.java, Float::class.java, Float::class.java)

    override fun wrap(target: Triple<Float, Float, Float>): Vector3fWrapper {
        val vector3f = vector3fConstructor.newInstance(target.first, target.second, target.third)
        return Vector3fWrapper(vector3f, vector3fClass, reflectionUtil)
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