package kr.hqservice.framework.nms.legacy.wrapper.world

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.Location

class LegacyWorldWrapper(
    private val world: Any,
    reflectionWrapper: LegacyNmsReflectionWrapper
) : WorldWrapper() {
    private val worldClass = reflectionWrapper.getNmsClass("World",
        Version.V_17.handle("world.level")
    )

    private val blockPositionClass = reflectionWrapper.getNmsClass("BlockPosition",
        Version.V_17.handle("core")
    )

    private val getBlockStateFunction = reflectionWrapper.getFunction(worldClass, "getBlockState", listOf(blockPositionClass),
        Version.V_17.handleFunction("a_") { setParameterClasses(blockPositionClass) },
        Version.V_17_FORGE.handleFunction("m_8055_") { setParameterClasses(blockPositionClass) }
    )

    override fun getBlockPosition(location: Location): Any {
        val intClass = Int::class.java
        return blockPositionClass.java.getConstructor(intClass, intClass, intClass)
            .newInstance(location.blockX, location.blockY, location.blockZ) ?: throw NullPointerException()
    }

    override fun getIBlockData(blockPosition: Any): Any {
        return getBlockStateFunction.call(world, blockPosition) ?: throw NullPointerException()
    }

    override fun getUnwrappedInstance(): Any {
        return world
    }
}