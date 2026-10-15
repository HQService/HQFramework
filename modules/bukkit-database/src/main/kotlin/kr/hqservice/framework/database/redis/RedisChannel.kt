package kr.hqservice.framework.database.redis

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisChannel(val value: String)
