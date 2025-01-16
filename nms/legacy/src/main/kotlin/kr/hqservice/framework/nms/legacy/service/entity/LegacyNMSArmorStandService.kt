package kr.hqservice.framework.nms.legacy.service.entity

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.math.LegacyVector3fWrapper
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.service.entity.NmsArmorStandService
import kr.hqservice.framework.nms.wrapper.entity.NmsArmorStandWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.math.Vector3fWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.Location
import org.bukkit.World
import kotlin.reflect.KClass

@Service
class LegacyNMSArmorStandService(
    private val reflectionWrapper: LegacyNmsReflectionWrapper,
    @Qualifier("vector3f") private val vector3fService: NmsService<Triple<Float, Float, Float>, Vector3fWrapper>,
    @Qualifier("nms.world") private val worldService: NmsService<World, WorldWrapper>,
) : NmsArmorStandService {

    private val armorStandClass = reflectionWrapper.getNmsClass("EntityArmorStand",
        Version.V_17.handle("world.entity.decoration")
    )

    private val armorStandConstructor = armorStandClass.java.getConstructor(
        worldService.getTargetClass().java, Double::class.java, Double::class.java, Double::class.java
    )

    private val setHeadPoseFunction = reflectionWrapper.getFunction(armorStandClass, "setHeadPose", listOf(vector3fService.getTargetClass()),
        Version.V_18.handleFunction("a") { setParameterClasses(vector3fService.getTargetClass()) },
        Version.V_17_FORGE.handleFunction("m_31597_") { setParameterClasses(vector3fService.getTargetClass()) }
    )

    private val getHeadPoseFunction = reflectionWrapper.getFunction(armorStandClass, "getHeadPose",
        Version.V_17.handleFunction("v"),
        Version.V_18.handleFunction("u"),
        Version.V_19_4.handleFunction("x"),
        Version.V_20_2.handleFunction("z"),
        Version.V_20_4.handleFunction("B"),
        Version.V_17_FORGE.handleFunction("m_31680_")
    )

    private val setSmallFunction = reflectionWrapper.getFunction(armorStandClass, "setSmall", listOf(Boolean::class),
        Version.V_18.handleFunction("a") { setParameterClasses(Boolean::class) },
        Version.V_19_4.handleFunction("t") { setParameterClasses(Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_31603_") { setParameterClasses(Boolean::class) }
    )

    private val setMarkerFunction = reflectionWrapper.getFunction(armorStandClass, "setMarker", listOf(Boolean::class),
        Version.V_18.handleFunction("t") { setParameterClasses(Boolean::class) },
        Version.V_19_4.handleFunction("u") { setParameterClasses(Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_31681_") { setParameterClasses(Boolean::class) }
    )

    private val setShowArmsFunction = reflectionWrapper.getFunction(armorStandClass, "setArms", listOf(Boolean::class),
        Version.V_18.handleFunction("r") { setParameterClasses(Boolean::class) },
        Version.V_19_4.handleFunction("a") { setParameterClasses(Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_31675_") { setParameterClasses(Boolean::class) }
    )

    private val setNoBasePlateFunction = reflectionWrapper.getFunction(armorStandClass, "setBasePlate", listOf(Boolean::class),
        Version.V_18.handleFunction("s") { setParameterClasses(Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_31678_") { setParameterClasses(Boolean::class) }
    )

    override fun wrap(target: Location): NmsArmorStandWrapper {
        val bukkitWorld = target.world ?: throw NullPointerException("world is null")
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

    override fun getHeadPose(wrapper: NmsArmorStandWrapper): Vector3fWrapper {
        val headPose =
            getHeadPoseFunction.call(wrapper.getUnwrappedInstance()) ?: throw NullPointerException("head-pose is null")
        return LegacyVector3fWrapper(headPose, vector3fService.getTargetClass(), reflectionWrapper)
    }

    override fun setHeadPose(wrapper: NmsArmorStandWrapper, triple: Triple<Float, Float, Float>) {
        val vector3f = vector3fService.wrap(triple).getUnwrappedInstance()
        setHeadPoseFunction.call(wrapper.getUnwrappedInstance(), vector3f)
    }

    override fun setSmall(wrapper: NmsArmorStandWrapper, small: Boolean) {
        setSmallFunction.call(wrapper.getUnwrappedInstance(), small)
    }

    override fun setMarker(wrapper: NmsArmorStandWrapper, marker: Boolean) {
        setMarkerFunction.call(wrapper.getUnwrappedInstance(), marker)
    }

    override fun setArms(wrapper: NmsArmorStandWrapper, arms: Boolean) {
        setShowArmsFunction.call(wrapper.getUnwrappedInstance(), arms)
    }

    override fun setBasePlate(wrapper: NmsArmorStandWrapper, basePlate: Boolean) {
        setNoBasePlateFunction.call(wrapper.getUnwrappedInstance(), !basePlate)
    }
}