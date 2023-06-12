package kr.hqservice.framework.database.util

import kr.hqservice.framework.bukkit.core.extension.toByteArray
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

fun ByteArray.toExposedBlob(): ExposedBlob {
    return ExposedBlob(this)
}

fun ItemStack.toExposedBlob(): ExposedBlob {
    return this.toByteArray().toExposedBlob()
}