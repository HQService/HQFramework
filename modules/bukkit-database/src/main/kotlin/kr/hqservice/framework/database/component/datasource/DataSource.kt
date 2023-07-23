package kr.hqservice.framework.database.component.datasource

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class DataSource(val configPath: String = "database.type")
