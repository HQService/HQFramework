package kr.hqservice.framework.command

import kotlin.reflect.KClass

class RegisteredCommandRoot(
    declaredAt: KClass<*>,
    label: String,
    permission: String,
    isOp: Boolean,
    hideSuggestion: Boolean
) : RegisteredCommandTree(declaredAt, label, 0, permission, isOp, hideSuggestion)