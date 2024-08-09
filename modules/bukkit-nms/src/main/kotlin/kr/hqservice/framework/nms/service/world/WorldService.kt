package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import net.minecraft.server.level.ServerLevel
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import kotlin.reflect.KClass

@Qualifier("nms.world")
@Service
class WorldService : NmsService<World, WorldWrapper> {
    override fun wrap(target: World): WorldWrapper {
        val craftWorld = target as CraftWorld
        val worldServer = craftWorld.handle
        return WorldWrapper(worldServer)
    }

    override fun unwrap(wrapper: WorldWrapper): World {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return World::class
    }

    override fun getTargetClass(): KClass<*> {
        return ServerLevel::class
    }
}