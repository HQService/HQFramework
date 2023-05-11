package kr.hqservice.framework.test

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Isolated(val testName: String)
