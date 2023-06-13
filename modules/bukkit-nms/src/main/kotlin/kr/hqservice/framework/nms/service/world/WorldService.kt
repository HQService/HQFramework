package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import org.bukkit.World
import org.koin.core.annotation.Named
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Component
@Named("world")
@HQSingleton(binds = [NmsService::class])
class WorldService(
    reflectionUtil: NmsReflectionWrapper
) : NmsService<World, WorldWrapper> {
    private val craftWorldClass = reflectionUtil.getCraftBukkitClass("CraftWorld")
    private val getHandleFunction = reflectionUtil.getFunction(craftWorldClass, "getHandle")
    private val worldClass = reflectionUtil.getNmsClass("World",
        Version.V_15.handle("world.level"))

    override fun wrap(target: World): WorldWrapper {
        val craftWorld = craftWorldClass.cast(target)
        return WorldWrapper(getHandleFunction.call(craftWorld)?: throw IllegalStateException("could not wrapping ${target::class.simpleName} class"))
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