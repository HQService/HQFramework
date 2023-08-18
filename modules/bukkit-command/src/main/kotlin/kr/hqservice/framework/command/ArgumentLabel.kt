package kr.hqservice.framework.command

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ArgumentLabel(val label: String)
