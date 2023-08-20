package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldBoarderWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.World
import kotlin.reflect.KClass

@Qualifier("nms.world.boarder")
@Service
class WorldBoardService(
    private val reflectionWrapper: NmsReflectionWrapper,
    @Qualifier("nms.world") private val worldService: NmsService<World, WorldWrapper>
) : NmsService<World, WorldBoarderWrapper> {
    private val worldBoarderClass = reflectionWrapper.getNmsClass("WorldBoarder",
        Version.V_15.handle("world.level.boarder"))
    private val worldBoarderConstructor = worldBoarderClass.constructors.first { it.parameters.isEmpty() }
    private val worldField = worldBoarderClass.java.getField("world")

    override fun wrap(target: World): WorldBoarderWrapper {
        val worldBoarderInst = worldBoarderConstructor.call()
        val worldWrapper = worldService.wrap(target)
        worldField.set(worldBoarderInst, worldWrapper.getUnwrappedInstance())
        return WorldBoarderWrapper(worldBoarderInst, reflectionWrapper, worldBoarderClass)
    }

    override fun unwrap(wrapper: WorldBoarderWrapper): World {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return World::class
    }

    override fun getTargetClass(): KClass<*> {
        return worldBoarderClass
    }
}