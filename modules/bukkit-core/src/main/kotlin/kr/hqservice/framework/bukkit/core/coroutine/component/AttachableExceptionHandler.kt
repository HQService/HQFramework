package kr.hqservice.framework.bukkit.core.coroutine.component

interface AttachableExceptionHandler {
    val priority: Int

    /**
     * @return 핸들 되었는지 여부를 반환합니다.
     */
    fun handle(throwable: Throwable): HandleResult
}