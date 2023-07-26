package kr.hqservice.framework.yaml.exception

import java.util.logging.Logger

class ConfigNotDefinedException(val key: String) : Exception() {
    companion object {
        fun handleException(logger: Logger, throwable: Throwable) {
            if (throwable is ConfigNotDefinedException) {
                logger.severe("${throwable.key} 키의 config 값을 찾을 수 없습니다.")
            }
        }
    }
}