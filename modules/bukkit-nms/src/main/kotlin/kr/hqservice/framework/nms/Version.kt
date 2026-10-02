package kr.hqservice.framework.nms

import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.handler.VersionHandler
import kr.hqservice.framework.nms.handler.impl.CallableVersionHandler
import kr.hqservice.framework.nms.handler.impl.NameVersionHandler

enum class Version(
    private val parent: Version? = null
) {
    V_17,
    V_18,
    V_18_2,
    V_19,
    V_19_1,
    V_19_2,
    V_19_3,
    V_19_4,
    V_20,
    V_20_1,
    V_20_2,
    V_20_3,
    V_20_4,
    V_20_6,
    V_21,
    V_21_1,
    V_21_3,
    V_21_4,
    V_21_5,
    V_21_6,
    V_21_7,
    V_21_8,
    V_21_11,
    V_26_1,
    V_26_2,

    // forge
    V_17_FORGE(parent= V_17),
    V_19_FORGE(parent= V_19),
    V_20_2_FORGE(parent= V_20_2);

    companion object {
        private const val NEW_NAMING_MAJOR = 26

        fun resolve(versionName: String): Version? {
            val (major, minor) = parse(versionName) ?: return null
            val requestedLegacy = major < V_20_4.major || (major == V_20_4.major && minor <= V_20_4.minor)
            return entries
                .filter { !it.isForge() && it.major == major && it.minor <= minor }
                .filter { requestedLegacy || it.ordinal > V_20_4.ordinal }
                .maxByOrNull { it.minor }
        }

        fun supportedVersionNames(): List<String> {
            return entries.filterNot { it.isForge() }.map { it.displayName }
        }

        private fun parse(versionName: String): Pair<Int, Int>? {
            val parts = versionName.substringBefore("-").split(".").map { it.toIntOrNull() ?: return null }
            val first = parts.firstOrNull() ?: return null
            return if (first >= NEW_NAMING_MAJOR) first to (parts.getOrNull(1) ?: 0)
            else (parts.getOrNull(1) ?: return null) to (parts.getOrNull(2) ?: 0)
        }
    }

    private val nameParts: List<String> get() = name.split("_")

    val major: Int get() = nameParts[1].toInt()

    val minor: Int get() = nameParts.getOrNull(2)?.toIntOrNull() ?: 0

    val displayName: String
        get() = when {
            major >= NEW_NAMING_MAJOR -> "$major.$minor"
            minor == 0 -> "1.$major"
            else -> "1.$major.$minor"
        }

    fun matches(versionName: String): Boolean {
        return parse(versionName) == major to minor
    }

    private fun isForge(): Boolean = name.endsWith("_FORGE")

    fun support(version: Version, minor: Int = 0): Boolean {
        val serverOrdinal = if (minor != 0) try {
            Version.valueOf("${version.name}_$minor").ordinal
        } catch (_: Exception) {
            version.ordinal
        } else version.ordinal

        val targetOrdinal = parent?.ordinal ?: ordinal

        return serverOrdinal >= targetOrdinal
    }

    fun handle(name: String, changedName: Boolean = false): VersionHandler {
        return NameVersionHandler(this, name, changedName)
    }

    fun handleFunction(name: String, block: FunctionType.() -> Unit = {}): VersionHandler {
        if (name.isEmpty()) throw IllegalArgumentException("method without name")

        val type = FunctionType(name)
        block(type)
        return CallableVersionHandler(this, type)
    }
}