package kr.hqservice.framework.nms.service.impl.entity

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import org.bukkit.Location
import org.koin.core.annotation.Named
import kotlin.reflect.KClass

@Component
@Named("entity-armorstand")
@HQSingleton(binds = [NmsService::class])
class NmsArmorStandService(
    private val reflectionUtil: NmsReflectionUtil
) : NmsService<Location, NmsArmorStandWrapper> {
    private val armorStandClass = reflectionUtil.getNmsClass("EntityArmorStand",
        Version.V_15.handle("world.entity.decoration"))

    override fun wrap(target: Location): NmsArmorStandWrapper {
        TODO("Not yet implemented")
    }

    override fun unwrap(wrapper: NmsArmorStandWrapper): Location {
        TODO("Not yet implemented")
    }

    override fun getOriginalClass(): KClass<*> {
        return Location::class
    }

    override fun getTargetClass(): KClass<*> {
        TODO("Not yet implemented")
    }
}