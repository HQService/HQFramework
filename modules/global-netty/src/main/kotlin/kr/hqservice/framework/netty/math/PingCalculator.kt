package kr.hqservice.framework.netty.math

class PingCalculator {
    private var lastIndex = 0
    private val pingArray = Array<Long>(10) { 0 }

    var lastPingAverage = -1L
        private set

    var lastPing = 0L
        private set

    fun process(ms: Long) {
        lastPing = ms
        if(lastIndex < 10)
            pingArray[lastIndex++] = ms
        else {
            var sum = 0L
            for (i in 0 until 10)
                sum += pingArray[i]
            lastPingAverage = sum / 10
            lastIndex = 0
            pingArray[lastIndex++] = ms
        }
    }

}