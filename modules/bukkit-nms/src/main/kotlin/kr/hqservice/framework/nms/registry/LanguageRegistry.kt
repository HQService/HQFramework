package kr.hqservice.framework.nms.registry

import com.google.gson.Gson
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

@Component
@HQSingleton(binds = [LanguageRegistry::class])
class LanguageRegistry : HQSimpleComponent {
    private val languageMap = mutableMapOf<Locale, Map<String, String>>()

    @Suppress("unchecked_cast")
    fun registerLanguage(stream: InputStream, locale: Locale = Locale.KOREA) {
        stream.use {
            val reader = InputStreamReader(it, StandardCharsets.UTF_8)
            languageMap[locale] = Gson().fromJson(reader, Map::class.java) as Map<String, String>
        }
    }

    fun getLocalizeValue(descriptionKey: String, locale: Locale = Locale.KOREA): String? {
        return languageMap[locale]?.get(descriptionKey)
    }
}