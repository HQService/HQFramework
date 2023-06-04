package kr.hqservice.framework.netty.api

import java.util.*

interface NettyPlayer : MessageSender {
    fun getName(): String

    fun getUniqueId(): UUID

    fun getChannel(): NettyChannel?
}