package kr.hqservice.framework.core.component

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class QualifierProvider(val key: String)