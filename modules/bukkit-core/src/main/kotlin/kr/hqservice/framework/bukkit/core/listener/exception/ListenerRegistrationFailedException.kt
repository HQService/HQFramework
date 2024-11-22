package kr.hqservice.framework.bukkit.core.listener.exception

import kotlin.reflect.KClass

class ListenerRegistrationFailedException(kClass: KClass<*>, reason: String) : RuntimeException("Registration of listener ${kClass.simpleName} was failed. reason: $reason")