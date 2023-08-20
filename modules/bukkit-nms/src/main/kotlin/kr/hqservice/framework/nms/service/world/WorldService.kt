package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.World
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Qualifier("nms.world")
@Service
class WorldService(
    reflectionWrapper: NmsReflectionWrapper
) : NmsService<World, WorldWrapper> {
    private val craftWorldClass = reflectionWrapper.getCraftBukkitClass("CraftWorld")
    private val getHandleFunction = reflectionWrapper.getFunction(craftWorldClass, "getHandle")
    private val worldClass = reflectionWrapper.getNmsClass(
        "World",
        Version.V_15.handle("world.level")
    )

    override fun wrap(target: World): WorldWrapper {
        val craftWorld = craftWorldClass.cast(target)
        return WorldWrapper(
            getHandleFunction.call(craftWorld)
                ?: throw IllegalStateException("could not wrapping ${target::class.simpleName} class")
        )
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