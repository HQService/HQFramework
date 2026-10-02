package kr.hqservice.framework.netty.pipeline

import io.netty.handler.timeout.ReadTimeoutHandler
import java.util.concurrent.TimeUnit

class TimeOutHandler(
    timeout: Long,
    unit: TimeUnit
) : ReadTimeoutHandler(timeout, unit)