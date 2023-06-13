package kr.hqservice.framework.nms.extension

import kotlin.experimental.and
import kotlin.experimental.xor

infix fun Byte.mask(maskByte: Byte): Boolean {
    return and(maskByte) == maskByte
}

infix fun Byte.switch(maskByte: Byte): Byte {
    return xor(maskByte)
}