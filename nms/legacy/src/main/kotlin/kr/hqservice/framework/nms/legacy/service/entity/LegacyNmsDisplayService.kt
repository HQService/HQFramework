package kr.hqservice.framework.nms.legacy.service.entity

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.service.entity.NmsDisplayService
import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.virtual.entity.AbstractVirtualDisplay
import kr.hqservice.framework.nms.wrapper.entity.NmsDisplayWrapper
import org.bukkit.Location
import java.lang.reflect.Constructor
import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible

class LegacyNmsDisplayService(
    private val worldService: NmsWorldService,
    private val reflectionWrapper: LegacyNmsReflectionWrapper
) : NmsDisplayService {
    private val displayClass = reflectionWrapper.getNmsClass("Display",
        Version.V_19.handle("world.entity")
    )
    private val entityTypeClass = reflectionWrapper.getNmsClass("EntityTypes",
        Version.V_19.handle("world.entity")
    )
    val textDisplayType = reflectionWrapper.getStaticField(entityTypeClass, "TEXT_DISPLAY",
        Version.V_19.handleFunction("aX"),
        Version.V_20_4.handleFunction("aY"),
        Version.V_19_FORGE.handleFunction("f_268607_")
    ).call()!!

    internal val billdboardClass = reflectionWrapper.getNmsClass("Display\$BillboardConstraints",
        Version.V_19.handle("world.entity")
    )
    private val transformationClass = Class.forName("com.mojang.math.Transformation").kotlin
    private val dataWatcherClass = reflectionWrapper.getNmsClass("DataWatcher",
        Version.V_19.handle("network.syncher")
    )
    private val dataAccessorClass = reflectionWrapper.getNmsClass("DataWatcherObject",
        Version.V_19.handle("network.syncher")
    )

    private val dataWatcherSetFunction = reflectionWrapper.getFunction(dataWatcherClass, "set", listOf(dataAccessorClass, Any::class),
        Version.V_19.handleFunction("b") { setParameterClasses(dataAccessorClass, Any::class) },
        Version.V_19_FORGE.handleFunction("m_135381_") { setParameterClasses(dataAccessorClass, Any::class) }
    )
    private val getEntityDataFunction = reflectionWrapper.getFunction(displayClass, "getEntityData",
        Version.V_19.handle("aj"),
        Version.V_20_2.handle("al"),
        Version.V_20_4.handle("an"),
        Version.V_19_FORGE.handle("m_20088_")
    )
    private val DATA_INTERPOLATION_DURATION_ID = reflectionWrapper.getStaticField(displayClass, "DATA_INTERPOLATION_DURATION_ID",
        Version.V_19.handle("r"),
        Version.V_20.handle("q"),
        Version.V_20_2.handle("r"),
        Version.V_19_FORGE.handle("f_268449_"),
        Version.V_20_2_FORGE.handle("f_290650_")
    ).run {
        isAccessible = true
        call()!!
    }
    private val DATA_INTERPOLATION_START_DELTA_TICKS_ID = reflectionWrapper.getStaticField(displayClass, "DATA_INTERPOLATION_START_DELTA_TICKS_ID",
        Version.V_19.handle("q"),
        Version.V_20.handle("p"),
        Version.V_20_2.handle("q"),
        Version.V_19_FORGE.handle("f_276329_"),
        Version.V_20_2_FORGE.handle("f_291687_")
    ).run {
        isAccessible = true
        call()!!
    }
    private val setPosRawFunction = reflectionWrapper.getFunction(displayClass, "setPosRaw", listOf(Double::class, Double::class, Double::class),
        Version.V_19.handleFunction("p") { setParameterClasses(Double::class, Double::class, Double::class) },
        Version.V_19_FORGE.handleFunction("m_20343_") { setParameterClasses(Double::class, Double::class, Double::class) }
    )
    // head -> n , body -> o
    private val setYRotFunction = reflectionWrapper.getFunction(displayClass, "setYHeadRot", listOf(Float::class),
        Version.V_19.handleFunction("r") { setParameterClasses(Float::class) },
        Version.V_20.handleFunction("n") { setParameterClasses(Float::class) },
        Version.V_19_FORGE.handleFunction("m_5616_") { setParameterClasses(Float::class) }
    )
    private val setTransformationFunction = reflectionWrapper.getFunction(displayClass, "setTransformation", listOf(transformationClass),
        Version.V_19.handleFunction("a") { setParameterClasses(transformationClass) },
        Version.V_19_FORGE.handleFunction("m_269214_") { setParameterClasses(transformationClass) }
    )
    private val setBillboardFunction = reflectionWrapper.getFunction(displayClass, "setBillboardConstraints", listOf(
        billdboardClass),
        Version.V_19.handleFunction("a") { setParameterClasses(billdboardClass) },
        Version.V_19_FORGE.handleFunction("m_269423_") { setParameterClasses(billdboardClass) }
    )
    private val setViewRangeFunction = reflectionWrapper.getFunction(displayClass, "setViewRange", listOf(Float::class),
        Version.V_19.handleFunction("g") { setParameterClasses(Float::class) },
        Version.V_20.handleFunction("s") { setParameterClasses(Float::class) },
        Version.V_20_2.handleFunction("b") { setParameterClasses(Float::class) },
        Version.V_19_FORGE.handleFunction("m_269215_") { setParameterClasses(Float::class) }
    )
    private val setShadowRadiusFunction = reflectionWrapper.getFunction(displayClass, "setShadowRadius", listOf(Float::class),
        Version.V_19.handleFunction("h") { setParameterClasses(Float::class) },
        Version.V_20.handleFunction("t") { setParameterClasses(Float::class) },
        Version.V_20_2.handleFunction("c") { setParameterClasses(Float::class) },
        Version.V_19_FORGE.handleFunction("m_269526_") { setParameterClasses(Float::class) }
    )

    override fun initializeLocation(virtualDisplay: AbstractVirtualDisplay, location: Location) {
        setPosRawFunction.call(virtualDisplay.getEntity(), location.x, location.y, location.z)
        setYRotFunction.call(virtualDisplay.getEntity(), location.yaw)
    }

    fun setEntityData(nmsWrapper: NmsDisplayWrapper, watcherObject: Any, obj: Any) {
        val data = getEntityDataFunction.call(nmsWrapper.getUnwrappedInstance())
        dataWatcherSetFunction.call(data, watcherObject, obj)
    }

    override fun applyTransformation(virtualDisplay: AbstractVirtualDisplay) {
        setTransformationFunction.call(virtualDisplay.getEntity(), virtualDisplay.transformation.getUnwrappedInstance())
    }

    override fun applyInterpolationDuration(virtualDisplay: AbstractVirtualDisplay) {
        val data = getEntityDataFunction.call(virtualDisplay.getEntity())
        dataWatcherSetFunction.call(data, DATA_INTERPOLATION_DURATION_ID, virtualDisplay.interpolationDuration)
    }

    override fun applyInterpolationDelay(virtualDisplay: AbstractVirtualDisplay) {
        val data = getEntityDataFunction.call(virtualDisplay.getEntity())
        dataWatcherSetFunction.call(data, DATA_INTERPOLATION_START_DELTA_TICKS_ID, virtualDisplay.interpolationDuration)
    }

    override fun applyBillboard(virtualDisplay: AbstractVirtualDisplay) {
        setBillboardFunction.call(virtualDisplay.getEntity(), virtualDisplay.billboard.getUnwrappedInstance())
    }

    override fun applyViewRange(virtualDisplay: AbstractVirtualDisplay) {
        setViewRangeFunction.call(virtualDisplay.getEntity(), virtualDisplay.viewDistance)
    }

    override fun applyShadowRadius(virtualDisplay: AbstractVirtualDisplay) {
        setShadowRadiusFunction.call(virtualDisplay.getEntity(), virtualDisplay.shadowRadius)
    }

    override fun getOriginalClass(): KClass<*> {
        return Location::class
    }

    override fun getTargetClass(): KClass<*> {
        return displayClass
    }

    fun getDisplayClass(nmsTypeName: String): KClass<*> {
        return reflectionWrapper.getNmsClass("Display\$${nmsTypeName}",
            Version.V_19.handle("world.entity")
        )
    }

    fun getConstructor(typeClass: KClass<*>, nmsTypeName: String): Constructor<*> {
        return getDisplayClass(nmsTypeName).java.getConstructor(typeClass.java, worldService.getTargetClass().java)
    }

    fun createTextDisplay(location: Location, nmsTypeName: String): Any {
        val cons = getConstructor(textDisplayType::class, nmsTypeName)

        val level = worldService.wrap(location.world!!).getUnwrappedInstance()
        return cons.newInstance(textDisplayType, level)
    }

    override fun wrap(target: Location): NmsDisplayWrapper {
        throw UnsupportedOperationException("")
    }

    override fun unwrap(wrapper: NmsDisplayWrapper): Location {
        throw UnsupportedOperationException("")
    }
}