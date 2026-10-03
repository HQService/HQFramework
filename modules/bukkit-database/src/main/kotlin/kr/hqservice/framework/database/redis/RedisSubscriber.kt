package kr.hqservice.framework.database.redis

import kr.hqservice.framework.global.core.component.Scannable

@Scannable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisSubscriber
