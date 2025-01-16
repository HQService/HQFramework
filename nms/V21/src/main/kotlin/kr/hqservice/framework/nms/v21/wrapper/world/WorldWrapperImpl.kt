package kr.hqservice.framework.nms.v21.wrapper.world

import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location

class WorldWrapperImpl(
    private val world: Level
) : WorldWrapper() {
    override fun getBlockPosition(location: Location): BlockPos {
        return BlockPos(location.blockX, location.blockY, location.blockZ)
    }

    override fun getIBlockData(blockPosition: Any): BlockState {
        return world.getBlockState(blockPosition as BlockPos)
    }

    override fun getUnwrappedInstance(): Any {
        return world
    }
}