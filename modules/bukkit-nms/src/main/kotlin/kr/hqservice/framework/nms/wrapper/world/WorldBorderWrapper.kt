package kr.hqservice.framework.nms.wrapper.world

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.hqservice.framework.coroutine.extension.BukkitMain
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kotlin.reflect.KClass

class WorldBorderWrapper(
    private val worldBorder: Any,
    reflectionWrapper: NmsReflectionWrapper,
    worldBorderClass: KClass<*>
) : NmsWrapper {
    private val setCenterFunction = reflectionWrapper.getFunction(worldBorderClass, FunctionType("setCenter", null, listOf(Double::class, Double::class)),
        Version.V_15.handleFunction("c") {
            setParameterClasses(Double::class, Double::class)
        })

    private val setSize = reflectionWrapper.getFunction(worldBorderClass, FunctionType("setSize", parameterClasses = listOf(Double::class)),
        Version.V_15.handleFunction("a") {
            setParameterClasses(Double::class)
        })

    suspend fun setCenter(x: Double, z: Double) {
        withContext(Dispatchers.BukkitMain) {
            setCenterFunction.call(worldBorder, x, z)
        }
    }

    fun setSize(size: Double) {
        if (size < 0) return
        setSize.call(worldBorder, size)
    }

    fun remove() {
        setSize.call(Int.MAX_VALUE)
    }

    override fun getUnwrappedInstance(): Any {
        return worldBorder
    }
}