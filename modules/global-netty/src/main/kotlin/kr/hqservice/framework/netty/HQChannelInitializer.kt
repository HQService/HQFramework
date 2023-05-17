package kr.hqservice.framework.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.PacketDecoder
import kr.hqservice.framework.netty.pipeline.PacketEncoder
import java.util.logging.Logger

class HQChannelInitializer(
    private val logger: Logger,
    private val server: Boolean = false
) : ChannelInitializer<NioSocketChannel>() {
    override fun initChannel(ch: NioSocketChannel) {
        if(server) ch.config().setOption(ChannelOption.TCP_NODELAY, true)
        ch.pipeline()
            .addLast("encode-filter", LengthFieldPrepender(8))
            .addLast("decode-filter", LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,8,0,8))
            .addLast("packet-decoder", PacketDecoder())
            .addLast("packet-encoder", PacketEncoder())
            .addLast("handler-boss", BossHandler(ch, logger))
    }
}