package kr.hqservice.framework.bukkit.core.extension

import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import kr.hqservice.framework.global.core.extension.throwIf
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun ItemStack.getDisplayName(): String {
    return if (this.itemMeta?.hasDisplayName() == true) this.itemMeta!!.displayName else this.type.name
}

fun ItemStack?.toByteArray(compress: Boolean = true): ByteArray {
    return arrayOf(this).toByteArray(compress)
}

fun ByteArray.toItemStack(decompress: Boolean = true): ItemStack {
    return toItemArray(decompress).throwIf("단일 ItemStack 이 아닙니다.") { it.size > 1 }.first()
}

fun Array<ItemStack?>.toByteArray(compress: Boolean = true): ByteArray {
    ByteArrayOutputStream().use {
        BukkitObjectOutputStream(it).use { outputStream ->
            outputStream.writeInt(size)
            map { itemStack -> itemStack?: ItemStack(Material.AIR) }
                .forEach(outputStream::writeObject)
        }
        return it.toByteArray()
            .run { if(compress) compress() else this }
    }
}

fun ByteArray.toItemArray(decompress: Boolean = true): Array<ItemStack> {
    ByteArrayInputStream(run {
        if(decompress) decompress()
        else this
    }).use {
        BukkitObjectInputStream(it).use { inputStream ->
            val size = inputStream.readInt()
            val list = mutableListOf<ItemStack>()
            for(i in 0 until size)
                list.add(inputStream.readObject() as ItemStack)
            return list.toTypedArray()
        }
    }
}

fun ItemStack.editMeta(block: ItemMeta.() -> Unit): ItemStack {
    itemMeta = itemMeta?.apply(block::invoke)
    return this
}

fun ItemMeta.addLine(line: String) {
    lore = lore?.apply {
        add(line)
    }?: mutableListOf(line)
}
