package kr.hqservice.framework.bukkit.core.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kr.hqservice.framework.global.core.extension.compress
import kr.hqservice.framework.global.core.extension.decompress
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor = PrimitiveSerialDescriptor("Bukkit.ItemStackSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ItemStack {
        val base64 = Base64Coder.decode(decoder.decodeString())
        val inputStream = ByteArrayInputStream(base64.decompress())
        val dataInput = BukkitObjectInputStream(inputStream)
        val itemStack = dataInput.readObject() as ItemStack
        dataInput.close()
        return itemStack
    }

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)
        dataOutput.writeObject(value)
        dataOutput.close()
        val serialized = Base64Coder.encode(outputStream.toByteArray().compress())
        val string = String(serialized)
        encoder.encodeString(string)
    }
}