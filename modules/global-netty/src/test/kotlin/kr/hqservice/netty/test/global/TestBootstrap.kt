package kr.hqservice.netty.test.global

import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import kr.hqservice.framework.netty.pipeline.BossHandler
import kr.hqservice.framework.netty.pipeline.PacketDecoder
import kr.hqservice.framework.netty.pipeline.PacketEncoder
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import java.util.logging.Logger

class TestBootstrap(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
) {
    fun initTestChannel(): EmbeddedChannel {
        val ch = EmbeddedChannel()
        ch.pipeline()
            .addLast("encode-filter", LengthFieldPrepender(8))
            .addLast("decode-filter", LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 8, 0, 8))
            .addLast("packet-decoder", PacketDecoder())
            .addLast("packet-encoder", PacketEncoder())
            .addLast("handler-boss", BossHandler(ch, logger))
        return ch
    }

}