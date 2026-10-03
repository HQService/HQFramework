package kr.hqservice.framework.database.repository.player.cache

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

interface PlayerDataCodec<V : Any> {
    fun encode(value: V): ByteArray

    fun decode(bytes: ByteArray): V
}

class JsonPlayerDataCodec<V : Any>(private val serializer: KSerializer<V>, private val json: Json) : PlayerDataCodec<V> {
    override fun encode(value: V): ByteArray = json.encodeToString(serializer, value).toByteArray()

    override fun decode(bytes: ByteArray): V = json.decodeFromString(serializer, bytes.decodeToString())
}
