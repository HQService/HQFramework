package kr.hqservice.framework.netty.packet

import kr.hqservice.framework.netty.channel.ChannelWrapper
import java.util.LinkedList
import kotlin.reflect.KClass

enum class Direction {
    INBOUND,
    OUTBOUND;

    protected val packetMap =
        mutableMapOf<KClass<out AbstractPacket>, PacketWrapper<out AbstractPacket>>()

    protected val handlers =
        mutableMapOf<KClass<out AbstractPacket>, LinkedList<PacketHandler<out AbstractPacket>>>()

    fun <T : AbstractPacket> registerPacket(packetClass: KClass<T>) {
        if (packetMap.containsKey(packetClass))
            throw IllegalArgumentException("packet duplicated")

        val constructor = packetClass.constructors.firstOrNull { it.parameters.isEmpty() }
            ?: throw IllegalArgumentException("packet has not define default constructor")

        packetMap[packetClass] = PacketWrapper(packetClass, constructor)
    }

    fun <T : AbstractPacket> addListener(packetClass: KClass<T>, packetHandler: (packet: T, channel: ChannelWrapper)-> Unit) {
        val handler = object: PacketHandler<T> {
            override fun onPacketReceive(packet: T, channel: ChannelWrapper) {
                packetHandler(packet, channel)
            }
        }
        handlers.computeIfAbsent(packetClass) { LinkedList() }.add(handler)
    }

    fun <T : AbstractPacket> addListener(packetClass: KClass<T>, packetHandler: PacketHandler<T>) {
        handlers.computeIfAbsent(packetClass) { LinkedList() }.add(packetHandler)
    }

    @Suppress("unchecked_cast")
    fun <T : AbstractPacket> getPacketByClass(clazz: KClass<T>): PacketWrapper<T> {
        return packetMap[clazz] as? PacketWrapper<T>?: throw IllegalArgumentException("not found packet")
    }

    @Suppress("unchecked_cast")
    fun <T : AbstractPacket> findPacketByClass(clazz: KClass<T>): PacketWrapper<T>? {
        return packetMap[clazz] as? PacketWrapper<T>
    }

    @Suppress("unchecked_cast")
    fun <T : AbstractPacket> onPacketReceived(packet: T, channel: ChannelWrapper): Boolean {
        val handlers = this.handlers[packet::class]?: return false
        for (listener in handlers) {
            listener as PacketHandler<T>
            listener.onPacketReceive(packet, channel)
        }
        return true
    }

}