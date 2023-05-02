package kr.hqservice.framework.core.component.error

import kotlin.reflect.KClass

class NotComponentException(klass: KClass<*>) : Exception("${klass.simpleName} is not HQComponent.")