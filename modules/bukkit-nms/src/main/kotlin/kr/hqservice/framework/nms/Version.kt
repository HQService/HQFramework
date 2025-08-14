package kr.hqservice.framework.nms

import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.handler.VersionHandler
import kr.hqservice.framework.nms.handler.impl.CallableVersionHandler
import kr.hqservice.framework.nms.handler.impl.NameVersionHandler

enum class Version(
    private val majorVersion: Int = -1,
    private val parent: Version? = null
) {
    V_17(17),
    V_18(18),
    V_18_2,
    V_19(19),
    V_19_1,
    V_19_2,
    V_19_3,
    V_19_4,
    V_20(20),
    V_20_1,
    V_20_2,
    V_20_3,
    V_20_4,
    V_20_6,
    V_21(21),
    V_21_1,
    V_21_3,
    V_21_4,
    V_21_5,
    V_21_6,

    // forge
    V_17_FORGE(parent= V_17),
    V_19_FORGE(parent= V_19),
    V_20_2_FORGE(parent= V_20_2);

    companion object {
        fun majorVersionOf(majorVersion: Int): Version? {
            return try { entries.first { it.majorVersion == majorVersion } } catch (_: Exception) { null }
        }
    }

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