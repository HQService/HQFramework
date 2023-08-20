package kr.hqservice.framework.nms.service.world

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldBorderWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.World
import kotlin.reflect.KClass

@Qualifier("nms.world.border")
@Service
class WorldBorderService(
    private val reflectionWrapper: NmsReflectionWrapper,
    @Qualifier("nms.world") private val worldService: NmsService<World, WorldWrapper>
) : NmsService<World, WorldBorderWrapper> {
    private val worldBorderClass = reflectionWrapper.getNmsClass("WorldBorder",
        Version.V_15.handle("world.level.border"))
    private val worldBorderConstructor = worldBorderClass.constructors.first { it.parameters.isEmpty() }
    private val worldField = worldBorderClass.java.getField("world")

    override fun wrap(target: World): WorldBorderWrapper {
        val worldBorderInst = worldBorderConstructor.call()
        val worldWrapper = worldService.wrap(target)
        worldField.set(worldBorderInst, worldWrapper.getUnwrappedInstance())
        return WorldBorderWrapper(worldBorderInst)
    }

    override fun unwrap(wrapper: WorldBorderWrapper): World {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return World::class
    }

    override fun getTargetClass(): KClass<*> {
        return worldBorderClass
    }
}