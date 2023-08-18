package kr.hqservice.framework.command

import kotlin.reflect.KClass

class RegisteredCommandRoot(declaredAt: KClass<*>, label: String) : RegisteredCommandTree(declaredAt, label)