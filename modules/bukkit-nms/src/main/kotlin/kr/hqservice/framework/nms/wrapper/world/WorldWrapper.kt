package kr.hqservice.framework.nms.wrapper.world

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location

class WorldWrapper(
    private val world: Level
) : NmsWrapper {
/*
    private val worldClass = reflectionWrapper.getNmsClass("World",
        Version.V_17.handle("world.level")
    )

    private val blockPositionClass = reflectionWrapper.getNmsClass("BlockPosition",
        Version.V_17.handle("core")
    )

    private val getBlockStateFunction = reflectionWrapper.getFunction(worldClass, "getBlockState", listOf(blockPositionClass),
        Version.V_17.handleFunction("a_") { setParameterClasses(blockPositionClass) },
        Version.V_17_FORGE.handleFunction("m_8055_") { setParameterClasses(blockPositionClass) }
    )*/

    fun getBlockPosition(location: Location): BlockPos {
        return BlockPos(location.blockX, location.blockY, location.blockZ)
    }

    fun getIBlockData(blockPosition: BlockPos): BlockState {
        return world.getBlockState(blockPosition)
    }

    override fun getUnwrappedInstance(): Any {
        return world
    }
}