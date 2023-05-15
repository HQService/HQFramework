package kr.hqservice.framework.netty

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.nio.NioEventLoopGroup

class ServerBootstrap {

    private val group = NioEventLoopGroup(0, ThreadFactoryBuilder().setNameFormat("HQ IO Thread #%1\$d").build())

    fun initializing(isBootUp: Boolean) {

    }

}