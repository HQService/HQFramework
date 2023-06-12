package kr.hqservice.framework.nms.service.entity

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.Location
import org.bukkit.World
import org.koin.core.annotation.Named
import kotlin.reflect.KClass

@Component
@Named("entity-armorstand")
@HQSingleton(binds = [NmsService::class])
class NmsArmorStandService(
    private val reflectionUtil: NmsReflectionUtil,
    @Named("vector3f") private val vector3fService: NmsService<Triple<Float, Float, Float>, Vector3fWrapper>,
    @Named("world") private val worldService: NmsService<World, WorldWrapper>
) : NmsService<Location, NmsArmorStandWrapper> {
    private val armorStandClass = reflectionUtil.getNmsClass("EntityArmorStand",
        Version.V_15.handle("world.entity.decoration"))

    private val armorStandConstructor = armorStandClass.java.getConstructor(
        worldService.getTargetClass().java, Double::class.java, Double::class.java, Double::class.java)

    private val getIdFunction = reflectionUtil.getFunction(armorStandClass, "getId",
        Version.V_15.handleFunction("S"),
        Version.V_17.handleFunction("Z"),
        Version.V_19.handleFunction("ae"),
        Version.V_19_3.handleFunction("ah"),
        Version.V_19_4.handleFunction("af")
    )

    private val setHeadPoseFunction = reflectionUtil.getFunction(armorStandClass, "setHeadPose", listOf(vector3fService.getTargetClass()),
        Version.V_15.handleFunction("a") { setParameterClasses(vector3fService.getTargetClass())} )

    private val getHeadPoseFunction = reflectionUtil.getFunction(armorStandClass, "getHeadPose",
        Version.V_15.handleFunction("r"),
        Version.V_17.handleFunction("v"),
        Version.V_19.handleFunction("u"),
        Version.V_19_4.handleFunction("x"))

    override fun wrap(target: Location): NmsArmorStandWrapper {
        val bukkitWorld = target.world?: throw NullPointerException("world is null")
        val nmsWorld = worldService.wrap(bukkitWorld).getUnwrappedInstance()
        val armorStandInstance = armorStandConstructor.newInstance(nmsWorld, target.x, target.y, target.z)
        val id = getIdFunction.call(armorStandInstance)
        return NmsArmorStandWrapper(id as Int, armorStandInstance, target, "", this)
    }

    override fun unwrap(wrapper: NmsArmorStandWrapper): Location {
        throw UnsupportedOperationException("")
    }

    override fun getOriginalClass(): KClass<*> {
        return Location::class
    }

    override fun getTargetClass(): KClass<*> {
        return armorStandClass
    }

    internal fun getHeadPose(wrapper: NmsArmorStandWrapper): Vector3fWrapper {
        val headPose = getHeadPoseFunction.call(wrapper.getNmsEntity())?: throw NullPointerException("head-pose is null")
        return Vector3fWrapper(headPose, vector3fService.getTargetClass(), reflectionUtil)
    }

    internal fun setHeadPose(wrapper: NmsArmorStandWrapper, triple: Triple<Float, Float, Float>) {
        val vector3f = vector3fService.wrap(triple).getUnwrappedInstance()
        setHeadPoseFunction.call(wrapper.getNmsEntity(), vector3f)
    }
}