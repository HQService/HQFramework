package kr.hqservice.framework.global.core.component.error

import kotlin.reflect.KClass

class NotComponentHandlerException(klass: KClass<*>) : Exception("${klass.simpleName} is not HQComponentHandler.")