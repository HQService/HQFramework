package kr.hqservice.framework.nms.wrapper.world

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kotlin.reflect.KClass

class WorldBoarderWrapper(
    private val worldBoarder: Any,
    reflectionWrapper: NmsReflectionWrapper,
    worldBoarderClass: KClass<*>
) : NmsWrapper {
    private val setCenterFunction = reflectionWrapper.getFunction(worldBoarderClass, FunctionType("setCenter", null, listOf(Double::class, Double::class)),
        Version.V_15.handleFunction("c") {
            setParameterClasses(Double::class, Double::class)
        })

    private val setSize = reflectionWrapper.getFunction(worldBoarderClass, FunctionType("setSize", parameterClasses = listOf(Int::class)),
        Version.V_15.handleFunction("a") {
            setParameterClasses(Int::class)
        })

    fun setCenter(x: Double, z: Double) {
        setCenterFunction.call(worldBoarder, x, z)
    }

    fun setSize(size: Int) {
        if (size < 0) return
        setSize.call(worldBoarder, size)
    }

    fun remove() {
        setSize.call(Int.MAX_VALUE)
    }

    override fun getUnwrappedInstance(): Any {
        return worldBoarder
    }
}