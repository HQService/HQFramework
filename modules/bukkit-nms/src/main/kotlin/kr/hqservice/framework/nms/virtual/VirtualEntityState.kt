package kr.hqservice.framework.nms.virtual

import kotlin.experimental.and
import kotlin.experimental.xor

enum class VirtualEntityState(
    val mask: Byte
) {
    CREAT(0x1),
    RELOCATE(0x2),
    UPDATE_META_DATA(0x4),
    DESTROY(0x8),
    UNHANDLED(0x10);
}

infix fun Byte.mask(state: VirtualEntityState): Boolean {
    val maskByte = state.mask
    return and(maskByte) == maskByte
}

infix fun Byte.switch(state: VirtualEntityState): Byte {
    val maskByte = state.mask
    return xor(maskByte)
}