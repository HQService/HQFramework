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
    private val reflectionWrapper: NmsReflectionWrapper
) : NmsService<World, WorldWrapper> {

    private val craftWorldClass = reflectionWrapper.getCraftBukkitClass("CraftWorld")
    private val getHandleFunction = reflectionWrapper.getFunction(craftWorldClass, "getHandle")
    private val worldClass = reflectionWrapper.getNmsClass("World",
        Version.V_17.handle("world.level")
    )

    override fun wrap(target: World): WorldWrapper {
        val craftWorld = craftWorldClass.cast(target)
        val worldServer = getHandleFunction.call(craftWorld) ?: throw IllegalStateException("could not wrapping ${target::class.simpleName} class")
        return WorldWrapper(worldServer, reflectionWrapper)
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