package kr.hqservice.framework.nms.service.entity

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsEntityService
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.Location
import org.bukkit.World
import org.koin.core.annotation.Named
import kotlin.reflect.KClass

@Component
@HQSingleton(binds = [NmsArmorStandService::class])
class NmsArmorStandService(
    private val reflectionWrapper: NmsReflectionWrapper,
    @Named("vector3f") private val vector3fService: NmsService<Triple<Float, Float, Float>, Vector3fWrapper>,
    @Named("world") private val worldService: NmsService<World, WorldWrapper>,
) : NmsEntityService<NmsArmorStandWrapper> {
    private val armorStandClass = reflectionWrapper.getNmsClass("EntityArmorStand",
        Version.V_15.handle("world.entity.decoration"))

    private val armorStandConstructor = armorStandClass.java.getConstructor(
        worldService.getTargetClass().java, Double::class.java, Double::class.java, Double::class.java)

    private val setHeadPoseFunction = reflectionWrapper.getFunction(armorStandClass, "setHeadPose", listOf(vector3fService.getTargetClass()),
        Version.V_15.handleFunction("a") { setParameterClasses(vector3fService.getTargetClass())} )

    private val getHeadPoseFunction = reflectionWrapper.getFunction(armorStandClass, "getHeadPose",
        Version.V_15.handleFunction("r"),
        Version.V_17.handleFunction("v"),
        Version.V_19.handleFunction("u"),
        Version.V_19_4.handleFunction("x"))

    private val setSmallFunction = reflectionWrapper.getFunction(armorStandClass, "setSmall", listOf(Boolean::class) ,
        Version.V_15.handleFunction("n") { setParameterClasses(Boolean::class)},
        Version.V_17.handleFunction("a") { setParameterClasses(Boolean::class)},
        Version.V_19_4.handleFunction("t") { setParameterClasses(Boolean::class)})

    override fun wrap(target: Location): NmsArmorStandWrapper {
        val bukkitWorld = target.world?: throw NullPointerException("world is null")
        val nmsWorld = worldService.wrap(bukkitWorld).getUnwrappedInstance()
        val armorStandInstance = armorStandConstructor.newInstance(nmsWorld, target.x, target.y, target.z)
        return NmsArmorStandWrapper(armorStandInstance, this)
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
        val headPose = getHeadPoseFunction.call(wrapper.getUnwrappedInstance())?: throw NullPointerException("head-pose is null")
        return Vector3fWrapper(headPose, vector3fService.getTargetClass(), reflectionWrapper)
    }

    internal fun setHeadPose(wrapper: NmsArmorStandWrapper, triple: Triple<Float, Float, Float>) {
        val vector3f = vector3fService.wrap(triple).getUnwrappedInstance()
        setHeadPoseFunction.call(wrapper.getUnwrappedInstance(), vector3f)
    }

    internal fun setSmall(wrapper: NmsArmorStandWrapper, small: Boolean) {
        setSmallFunction.call(wrapper.getUnwrappedInstance(), small)
    }
}