package kr.hqservice.framework.global.core.component.registry

interface MutableNamedProvider {
    fun provideQualifier(): String
}