package kr.hqservice.framework.netty.packet

import kotlinx.coroutines.CancellationException
import java.util.logging.Level
import java.util.logging.Logger
import kr.hqservice.framework.netty.channel.ChannelWrapper
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.MethodCall
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

private val logger: Logger = Logger.getLogger("HQFramework.netty")

enum class Direction {
    INBOUND,
    OUTBOUND;

    private val packetMap = ConcurrentHashMap<String, PacketWrapper<out Packet>>()
    private val handlers = ConcurrentHashMap<String, CopyOnWriteArrayList<PacketHandler<out Packet>>>()

    fun <T : Packet> unregisterPacket(packetClass: KClass<T>) {
        packetMap.remove(packetClass.qualifiedName)
        handlers.remove(packetClass.qualifiedName)
    }

    fun <T : Packet> registerPacket(packetClass: KClass<T>) {
        val existing = packetMap[packetClass.qualifiedName!!]
        if (existing != null && existing.clazz.java === packetClass.java)
            return

        val primaryConstructor = packetClass.primaryConstructor
            ?: throw IllegalArgumentException("'${packetClass.simpleName}' packet has not primary constructor")
        primaryConstructor.parameters.forEach { requireBackingField(packetClass, it.name) }

        val codecClass: Class<*> = ByteBuddy()
            .redefine(packetClass.java)
            .name(packetClass.qualifiedName!! + "\$codec")
            .defineConstructor(Visibility.PUBLIC)
            .intercept(MethodCall.invokeSuper())
            .make()
            .load(packetClass.java.classLoader, ClassLoadingStrategy.Default.WRAPPER)
            .loaded

        packetMap[packetClass.qualifiedName!!] = PacketWrapper(packetClass, codecClass, primaryConstructor)
    }

    private fun requireBackingField(packetClass: KClass<*>, name: String?) {
        val missing = name == null || runCatching { packetClass.java.getDeclaredField(name) }.isFailure
        if (missing) throw IllegalArgumentException("packet ${packetClass.qualifiedName} parameter $name needs a backing property")
    }

    fun <T : Packet> addListener(packetClass: KClass<T>, packetHandler: suspend (packet: T, channel: ChannelWrapper) -> Unit) {
        val handler = object : PacketHandler<T> {
            override suspend fun onPacketReceive(packet: T, channel: ChannelWrapper) {
                packetHandler(packet, channel)
            }
        }
        handlers.computeIfAbsent(packetClass.qualifiedName!!) { CopyOnWriteArrayList() }.add(handler)
    }

    fun <T : Packet> addListener(packetClass: KClass<T>, packetHandler: PacketHandler<T>) {
        handlers.computeIfAbsent(packetClass.qualifiedName!!) { CopyOnWriteArrayList() }.add(packetHandler)
    }

    @Suppress("unchecked_cast")
    fun <T : Packet> getPacketByClass(clazz: KClass<T>): PacketWrapper<T> {
        return packetMap[clazz.qualifiedName!!] as? PacketWrapper<T>
            ?: throw IllegalArgumentException("not found packet")
    }

    @Suppress("unchecked_cast")
    fun <T : Packet> findPacketByClass(clazz: KClass<T>): PacketWrapper<T>? {
        return packetMap[clazz.qualifiedName!!] as? PacketWrapper<T>
    }

    fun findPacketByName(name: String): PacketWrapper<out Packet>? = packetMap[name]

    @Suppress("unchecked_cast")
    suspend fun <T : Packet> onPacketReceived(packet: T, channel: ChannelWrapper): Boolean {
        val handlers = this.handlers[packet::class.qualifiedName!!] ?: return false
        for (listener in handlers) {
            listener as PacketHandler<T>
            try {
                listener.onPacketReceive(packet, channel)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "packet handler ${listener::class.qualifiedName} failed for ${packet::class.simpleName}", e)
            }
        }
        return true
    }

}
