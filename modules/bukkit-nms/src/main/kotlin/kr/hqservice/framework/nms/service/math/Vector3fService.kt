package kr.hqservice.framework.nms.service.math

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import net.minecraft.core.Rotations
import kotlin.reflect.KClass

@Qualifier("vector3f")
@Service
class Vector3fService : NmsService<Triple<Float, Float, Float>, Vector3fWrapper> {

    override fun wrap(target: Triple<Float, Float, Float>): Vector3fWrapper {
        val vector3f = Rotations(target.first, target.second, target.third)
        return Vector3fWrapper(vector3f)
    }

    override fun unwrap(wrapper: Vector3fWrapper): Triple<Float, Float, Float> {
        return Triple(wrapper.getX(), wrapper.getY(), wrapper.getZ())
    }

    override fun getOriginalClass(): KClass<*> {
        return Triple::class
    }

    override fun getTargetClass(): KClass<*> {
        return Rotations::class
    }
}