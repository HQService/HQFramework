package kr.hqservice.framework.netty.api

import java.util.*

interface NettyPlayer {
    fun getName(): String

    fun getUniqueId(): UUID

    fun getChannel(): NettyChannel?
}