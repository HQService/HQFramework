package kr.hqservice.framework.nms.legacy.service.world

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.world.LegacyWorldWrapper
import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.World
import kotlin.reflect.KClass
import kotlin.reflect.cast

class LegacyWorldService(
    private val reflectionWrapper: LegacyNmsReflectionWrapper
) : NmsWorldService {
    private val craftWorldClass = reflectionWrapper.getCraftBukkitClass("CraftWorld")
    private val getHandleFunction = reflectionWrapper.getFunction(craftWorldClass, "getHandle")
    private val worldClass = reflectionWrapper.getNmsClass("World",
        Version.V_17.handle("world.level")
    )

    override fun wrap(target: World): WorldWrapper {
        val craftWorld = craftWorldClass.cast(target)
        val worldServer = getHandleFunction.call(craftWorld) ?: throw IllegalStateException("could not wrapping ${target::class.simpleName} class")
        return LegacyWorldWrapper(worldServer, reflectionWrapper)
    }

    override fun unwrap(wrapper: WorldWrapper): World {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return World::class
    }

    override fun getTargetClass(): KClass<*> {
        return worldClass
    }
}