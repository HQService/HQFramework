package kr.hqservice.framework.nms.virtual.entity.display

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.joml.Quaternionf
import org.joml.Vector3f

class HQTransformation(
    private val translation: Vector3f = Vector3f(),
    private val leftRotation: Quaternionf = Quaternionf(),
    private val scale: Vector3f = Vector3f(1f),
    private val rightRotation: Quaternionf = Quaternionf()
) : NmsWrapper {
    companion object {
        private val transformationClass = Class.forName("com.mojang.math.Transformation")
        private val transformationConstructor = transformationClass.getConstructor(
            Vector3f::class.java,
            Quaternionf::class.java,
            Vector3f::class.java,
            Quaternionf::class.java
        )
    }

    private val instance = transformationConstructor.newInstance(translation, leftRotation, scale, rightRotation)

    fun getTranslation(): Vector3f {
        return translation
    }

    fun getLeftRotation(): Quaternionf {
        return leftRotation
    }

    fun getScale(): Vector3f {
        return scale
    }

    fun getRightRotation(): Quaternionf {
        return rightRotation
    }

    override fun getUnwrappedInstance(): Any {
        return instance
    }
}