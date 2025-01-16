package kr.hqservice.framework.nms

import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitComponentRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import org.bukkit.Server

//@ComponentHandler
class NMSServiceManagerHandler(
    server: Server,
    private val componentRegistry: BukkitComponentRegistry
) : HQComponentHandler<NMSServiceManager> {
    private val versionName = server.bukkitVersion
    private val majorVersion = versionName.split(".")[1].toInt()
    private val version = Version.majorVersionOf("V_$majorVersion")

    override fun setup(element: NMSServiceManager) {
        componentRegistry.getComponent(NMSServiceManager::class)
        if (version == null)
            throw UnsupportedOperationException("unsupported version: $versionName")

        if (element.support(version)) {
            println("SUPPORT VERSION $version <-------------------")
            element.initialize()

            NMSServiceManager.instance = element
        }
    }
}