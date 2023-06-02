package kr.hqservice.framework.netty.packet

import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.netty.channel.ChannelWrapper
import kr.hqservice.framework.netty.packet.server.HandShakePacket
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.MethodCall
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

enum class Direction {
    INBOUND,
    OUTBOUND;

    private val packetMap =
        mutableMapOf<String, PacketWrapper<out Packet>>()

    private val handlers =
        mutableMapOf<String, LinkedList<PacketHandler<out Packet>>>()

    fun <T : Packet> registerPacket(packetClass: KClass<T>) {
        if (packetMap.containsKey(packetClass.qualifiedName!!))
            throw IllegalArgumentException("packet duplicated")

        val codecClass: Class<*> = ByteBuddy()
            .redefine(packetClass.java)
            .name(packetClass.qualifiedName!! + "\$codec")
            .defineConstructor(Visibility.PUBLIC)
            .intercept(MethodCall.invokeSuper())
            .make()
            .load(packetClass.java.classLoader, ClassLoadingStrategy.Default.WRAPPER)
            .loaded

        val primaryConstructor = packetClass.primaryConstructor?: throw IllegalArgumentException("'${packetClass.simpleName}' packet has not primary constructor")

        packetMap[packetClass.qualifiedName!!] = PacketWrapper(packetClass, codecClass, primaryConstructor)
    }

    fun <T : Packet> addListener(packetClass: KClass<T>, packetHandler: (packet: T, channel: ChannelWrapper)-> Unit) {
        val handler = object: PacketHandler<T> {
            override fun onPacketReceive(packet: T, channel: ChannelWrapper) {
                packetHandler(packet, channel)
            }
        }
        handlers.computeIfAbsent(packetClass.qualifiedName!!) { LinkedList() }.add(handler)
    }

    fun <T : Packet> addListener(packetClass: KClass<T>, packetHandler: PacketHandler<T>) {
        handlers.computeIfAbsent(packetClass.qualifiedName!!) { LinkedList() }.add(packetHandler)
    }

    @Suppress("unchecked_cast")
    fun <T : Packet> getPacketByClass(clazz: KClass<T>): PacketWrapper<T> {
        return packetMap[clazz.qualifiedName!!] as? PacketWrapper<T>?: throw IllegalArgumentException("not found packet")
    }

    @Suppress("unchecked_cast")
    fun <T : Packet> findPacketByClass(clazz: KClass<T>): PacketWrapper<T>? {
        return packetMap[clazz.qualifiedName!!] as? PacketWrapper<T>
    }

    @Suppress("unchecked_cast")
    fun <T : Packet> onPacketReceived(packet: T, channel: ChannelWrapper): Boolean {
        val handlers = this.handlers[packet::class.qualifiedName!!]?: return false
        for (listener in handlers) {
            listener as PacketHandler<T>
            listener.onPacketReceive(packet, channel)
        }
        return true
    }

}