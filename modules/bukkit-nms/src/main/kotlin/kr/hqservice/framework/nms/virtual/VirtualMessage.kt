package kr.hqservice.framework.nms.virtual

import net.minecraft.network.protocol.Packet

interface VirtualMessage {
    fun send(block: (packet: Packet<*>) -> Unit)
}