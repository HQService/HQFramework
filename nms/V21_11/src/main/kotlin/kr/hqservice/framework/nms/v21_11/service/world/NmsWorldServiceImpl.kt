package kr.hqservice.framework.nms.v21_11.service.world

import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.v21_11.wrapper.world.WorldWrapperImpl
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import net.minecraft.server.level.ServerLevel
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import kotlin.reflect.KClass

class NmsWorldServiceImpl : NmsWorldService {
    override fun wrap(target: World): WorldWrapperImpl {
        val craftWorld = target as CraftWorld
        val worldServer = craftWorld.handle
        return WorldWrapperImpl(worldServer)
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