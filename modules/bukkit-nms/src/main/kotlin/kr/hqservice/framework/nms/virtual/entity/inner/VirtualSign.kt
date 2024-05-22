package kr.hqservice.framework.nms.virtual.entity.inner

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.getStaticFunction
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.World
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.reflect.full.cast

class VirtualSign(
    private val player: Player,
    private val reflectionWrapper: NmsReflectionWrapper
) : Virtual, KoinComponent {

    private val worldService: NmsService<World, WorldWrapper> by inject(named("nms.world"))
    // private val baseComponentService: NmsService<String, BaseComponentWrapper> by inject(named("base-component"))

    private val blockPositionClass = reflectionWrapper.getNmsClass("BlockPosition",
        Version.V_17.handle("core")
    )
    private val iBlockDataClass = reflectionWrapper.getNmsClass("IBlockData",
        Version.V_17.handle("world.level.block.state")
    )
    private val tileEntityClass = reflectionWrapper.getNmsClass("TileEntity",
        Version.V_17.handle("world.level.block.entity")
    )
    private val tileEntitySignClass = reflectionWrapper.getNmsClass("TileEntitySign",
        Version.V_17.handle("world.level.block.entity")
    )
    private val tileEntityTypeClass = reflectionWrapper.getNmsClass("TileEntityTypes",
        Version.V_17.handle("world.level.block.entity")
    )
    /*private val signTextClass = reflectionWrapper.getNmsClass("SignText",
        Version.V_17.handle("world.level.block.entity")
    )
    private val enumColorClass = reflectionWrapper.getNmsClass("EnumColor",
        Version.V_17.handle("world.item")
    )*/

    private val tileEntityDataPacketClass = reflectionWrapper.getNmsClass("PacketPlayOutTileEntityData",
        Version.V_17.handle("network.protocol.game")
    )
    private val signEditorPacketClass = reflectionWrapper.getNmsClass("PacketPlayOutOpenSignEditor",
        Version.V_17.handle("network.protocol.game")
    )

    private val tileEntityDataPacketCreateFunction = reflectionWrapper.getStaticFunction(tileEntityDataPacketClass, "create", listOf(tileEntityClass),
        Version.V_18.handleFunction("a") {
            setParameterClasses(tileEntityClass)
            static()
        }
    )
    /*private val enumColorValueOfFunction = reflectionWrapper.getStaticFunction(enumColorClass, "valueOf", listOf(String::class),
        Version.V_17.handleFunction("valueOf") { setParameterClasses(String::class) }
    )*/
    private val tileEntityCreateFunction = reflectionWrapper.getFunction(tileEntityTypeClass, "create", listOf(blockPositionClass, iBlockDataClass),
        Version.V_17.handleFunction("a") { setParameterClasses(blockPositionClass, iBlockDataClass) },
        Version.V_17_FORGE.handleFunction("m_155264_") { setParameterClasses(blockPositionClass, iBlockDataClass) }
    )
    /*private val tileEntitySignSetTextFunction = reflectionWrapper.getFunction(tileEntitySignClass, "setText", listOf(signTextClass, Boolean::class),
        Version.V_20.handleFunction("a") { setParameterClasses(signTextClass, Boolean::class) }
    )*/

    private val tileEntitySign = reflectionWrapper.getStaticField(tileEntityTypeClass, "SIGN",
        Version.V_17.handle("h"),
        Version.V_17_FORGE.handle("f_58924_")
    ).call()!!

    override fun createVirtualMessage(): VirtualMessage {
        val worldServer = worldService.wrap(player.world)
        val blockPosition = worldServer.getBlockPosition(player.location)
        val iBlockData = worldServer.getIBlockData(blockPosition)
        val tileEntity = tileEntityCreateFunction.call(tileEntitySign, blockPosition, iBlockData)
        val tileEntitySign = tileEntitySignClass.cast(tileEntity)
        /*val backComponents = Array(4) { getBaseComponent() }
        val frontComponents = Array(4) { getBaseComponent(texts[it]) }
        val black = enumColorValueOfFunction.call("BLACK")!!

        println()
        println(signTextClass.constructors.last())
        println()
        println("backComponents: ${backComponents::class} / ${backComponents::class.starProjectedType}")
        backComponents.forEachIndexed { index, it -> println("back$index: $it - ${it::class}") }
        println()
        println("frontComponents: ${frontComponents::class} / ${frontComponents::class.starProjectedType}")
        frontComponents.forEachIndexed { index, it -> println("front$index: $it - ${it::class}") }
        println()
        println("color: ${black::class}")
        println()

        val signText = signTextClass.constructors.first().call()//last().call(backComponents, frontComponents, black, false)
        tileEntitySignSetTextFunction.call(tileEntitySign, signText, true)
        Function<String, Int> {
            5
        }*/
        val tileEntityDataPacket = tileEntityDataPacketCreateFunction.call(tileEntitySign)!!
        val signEditorDataPacket = if (reflectionWrapper.getFullVersion().ordinal >= Version.V_20.ordinal) {
            signEditorPacketClass.java.getConstructor(blockPositionClass.java, Boolean::class.java)
                .newInstance(blockPosition, true)
        } else {
            signEditorPacketClass.java.getConstructor(blockPositionClass.java)
                .newInstance(blockPosition)
        }
        return VirtualListMessage(listOf(tileEntityDataPacket, signEditorDataPacket))
    }

    /*private fun getBaseComponent(text: String = ""): Any {
        return baseComponentService.wrap("{\"text\":\"$text\"}").getUnwrappedInstance()
    }*/
}