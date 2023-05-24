package kr.hqservice.framework.global.core.component.registry

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class QualifierProvider(val key: String)