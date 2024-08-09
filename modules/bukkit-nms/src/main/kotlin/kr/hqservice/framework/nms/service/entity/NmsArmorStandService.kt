package kr.hqservice.framework.nms.service.entity

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsEntityService
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import net.minecraft.core.Rotations
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import kotlin.reflect.KClass

@Service
class NmsArmorStandService(
    private val reflectionWrapper: NmsReflectionWrapper,
    @Qualifier("vector3f") private val vector3fService: NmsService<Triple<Float, Float, Float>, Vector3fWrapper>,
) : NmsEntityService<NmsArmorStandWrapper> {
    override fun wrap(target: Location): NmsArmorStandWrapper {
        val bukkitWorld = target.world ?: throw NullPointerException("world is null")
        val nmsWorld = (bukkitWorld as CraftWorld).handle
        val armorStandInstance = ArmorStand(nmsWorld, target.x, target.y, target.z)
        return NmsArmorStandWrapper(armorStandInstance, this)
    }

    override fun unwrap(wrapper: NmsArmorStandWrapper): Location {
        throw UnsupportedOperationException("")
    }

    override fun getOriginalClass(): KClass<*> {
        return Location::class
    }

    override fun getTargetClass(): KClass<*> {
        return ArmorStand::class
    }

    internal fun getHeadPose(wrapper: NmsArmorStandWrapper): Vector3fWrapper {
        val armorStand = wrapper.getUnwrappedInstance() as ArmorStand
        val headPose = armorStand.getHeadPose()
        return Vector3fWrapper(headPose)
    }

    internal fun setHeadPose(wrapper: NmsArmorStandWrapper, triple: Triple<Float, Float, Float>) {
        val vector3f = vector3fService.wrap(triple).getUnwrappedInstance() as Rotations
        (wrapper.getUnwrappedInstance() as ArmorStand).setHeadPose(vector3f)
    }

    internal fun setSmall(wrapper: NmsArmorStandWrapper, small: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).isSmall = small
    }

    internal fun setMarker(wrapper: NmsArmorStandWrapper, marker: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).isMarker = marker
    }

    internal fun setArms(wrapper: NmsArmorStandWrapper, arms: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).isShowArms = arms
    }

    internal fun setBasePlate(wrapper: NmsArmorStandWrapper, basePlate: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).isNoBasePlate = !basePlate
    }
}