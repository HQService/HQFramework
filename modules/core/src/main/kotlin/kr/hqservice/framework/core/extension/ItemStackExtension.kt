package kr.hqservice.framework.core.extension

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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