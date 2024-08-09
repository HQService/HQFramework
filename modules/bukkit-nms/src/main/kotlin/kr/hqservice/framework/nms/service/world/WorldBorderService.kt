package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.world.WorldBorderWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.WorldBorder
import org.bukkit.World
import kotlin.reflect.KClass

@Qualifier("nms.world.border")
@Service
class WorldBorderService(
    @Qualifier("nms.world") private val worldService: NmsService<World, WorldWrapper>
) : NmsService<World, WorldBorderWrapper> {
    override fun wrap(target: World): WorldBorderWrapper {
        val worldBorderInst = WorldBorder()
        val worldWrapper = worldService.wrap(target)
        worldBorderInst.world = worldWrapper.getUnwrappedInstance() as ServerLevel
        return WorldBorderWrapper(worldBorderInst)
    }

    override fun unwrap(wrapper: WorldBorderWrapper): World {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return World::class
    }

    override fun getTargetClass(): KClass<*> {
        return WorldBorder::class
    }
}