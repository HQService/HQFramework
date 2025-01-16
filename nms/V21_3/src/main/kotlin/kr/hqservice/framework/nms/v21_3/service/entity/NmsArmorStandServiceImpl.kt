package kr.hqservice.framework.nms.v21_3.service.entity

import kr.hqservice.framework.nms.service.entity.NmsArmorStandService
import kr.hqservice.framework.nms.service.math.NmsVector3fService
import kr.hqservice.framework.nms.v21.wrapper.math.Vector3fWrapperImpl
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import net.minecraft.core.Rotations
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import kotlin.reflect.KClass

class NmsArmorStandServiceImpl(
    private val vector3fService: NmsVector3fService,
) : NmsArmorStandService {
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

    override fun getHeadPose(wrapper: NmsArmorStandWrapper): Vector3fWrapper {
        val armorStand = wrapper.getUnwrappedInstance() as ArmorStand
        val headPose = armorStand.getHeadPose()
        return Vector3fWrapperImpl(headPose)
    }

    override fun setHeadPose(wrapper: NmsArmorStandWrapper, triple: Triple<Float, Float, Float>) {
        val vector3f = vector3fService.wrap(triple).getUnwrappedInstance() as Rotations
        (wrapper.getUnwrappedInstance() as ArmorStand).setHeadPose(vector3f)
    }

    override fun setSmall(wrapper: NmsArmorStandWrapper, small: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).isSmall = small
    }

    override fun setMarker(wrapper: NmsArmorStandWrapper, marker: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).isMarker = marker
    }

    override fun setArms(wrapper: NmsArmorStandWrapper, arms: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).setShowArms(arms)
    }

    override fun setBasePlate(wrapper: NmsArmorStandWrapper, basePlate: Boolean) {
        (wrapper.getUnwrappedInstance() as ArmorStand).setNoBasePlate(!basePlate)
    }
}