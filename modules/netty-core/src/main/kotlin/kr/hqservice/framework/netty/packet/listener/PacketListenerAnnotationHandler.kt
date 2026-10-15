package kr.hqservice.framework.netty.packet.listener

import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.Direction
import kr.hqservice.framework.netty.packet.Packet
import kr.hqservice.framework.netty.packet.PacketHandler
import java.lang.reflect.InvocationTargetException
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

@AnnotationHandler
class PacketListenerAnnotationHandler(private val logger: Logger) : HQAnnotationHandler<PacketListener> {
    private class Registration(val instance: Any, val packetClass: KClass<out Packet>, val handler: PacketHandler<Packet>)

    private val registrations = mutableListOf<Registration>()

    @Suppress("UNCHECKED_CAST")
    override fun setup(instance: Any, annotation: PacketListener) {
        val subscriptions = instance::class.declaredMemberFunctions
            .filter { it.findAnnotation<PacketSubscribe>() != null }
            .map { function -> function to packetClassOf(instance, function) }
        annotation.outbound.forEach { Direction.OUTBOUND.registerPacket(it) }
        subscriptions.forEach { (function, packetClass) ->
            function.isAccessible = true
            val withChannel = function.valueParameters.size == 2
            val handler = object : PacketHandler<Packet> {
                override suspend fun onPacketReceive(packet: Packet, channel: ChannelWrapper) {
                    invoke(function, instance, if (withChannel) arrayOf(packet, channel) else arrayOf(packet))
                }
            }
            Direction.INBOUND.registerPacket(packetClass)
            Direction.INBOUND.addListener(packetClass as KClass<Packet>, handler)
            registrations += Registration(instance, packetClass, handler)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun teardown(instance: Any, annotation: PacketListener) {
        registrations.filter { it.instance === instance }.forEach { Direction.INBOUND.removeListener(it.packetClass as KClass<Packet>, it.handler) }
        registrations.removeIf { it.instance === instance }
    }

    private fun packetClassOf(instance: Any, function: KFunction<*>): KClass<out Packet> {
        val parameters = function.valueParameters
        val packetClass = parameters.firstOrNull()?.type?.jvmErasure
        val valid = packetClass != null && packetClass.isSubclassOf(Packet::class) &&
            (parameters.size == 1 || (parameters.size == 2 && parameters[1].type.jvmErasure == ChannelWrapper::class))
        check(valid) { "@PacketSubscribe function ${instance::class.simpleName}.${function.name} must take (packet: Packet) or (packet: Packet, channel: ChannelWrapper)" }
        @Suppress("UNCHECKED_CAST")
        return packetClass as KClass<out Packet>
    }

    private suspend fun invoke(function: KFunction<*>, instance: Any, arguments: Array<Any>) {
        try {
            if (function.isSuspend) function.callSuspend(instance, *arguments) else function.call(instance, *arguments)
        } catch (exception: InvocationTargetException) {
            throw exception.cause ?: exception
        }
    }
}
