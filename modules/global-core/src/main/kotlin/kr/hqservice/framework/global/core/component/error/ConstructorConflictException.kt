package kr.hqservice.framework.global.core.component.error

import kotlin.reflect.KClass

class ConstructorConflictException(klass: KClass<*>) : Exception("${klass.simpleName} has more than one constructor.")