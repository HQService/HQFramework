package kr.hqservice.framework.test

@Repeatable
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Isolated(val testName: String)
