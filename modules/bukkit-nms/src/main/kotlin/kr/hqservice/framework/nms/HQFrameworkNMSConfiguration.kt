package kr.hqservice.framework.nms

import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.global.core.component.Configuration
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.service.entity.NmsArmorStandService
import kr.hqservice.framework.nms.service.entity.NmsDisplayService
import kr.hqservice.framework.nms.service.entity.NmsTextDisplayService
import kr.hqservice.framework.nms.service.item.NmsItemService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.service.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.service.math.NmsVector3fService
import kr.hqservice.framework.nms.service.world.NmsWorldBorderService
import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.util.NmsNettyInjectService
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.entity.display.HQBillboardFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualItemHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.virtual.item.VirtualItemMessageFactory
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Server

@Configuration
class HQFrameworkNMSConfiguration(
    private val plugin: HQBukkitPlugin,
    server: Server,
    serviceManager: NMSServiceManager
) : NMSServiceProvider, NMSVirtualFactoryProvider {
    private val versionName: String = server.minecraftVersion.split("-")[0]
    private val majorVersion = versionName.split(".")[1].toInt()
    private val minorVersion = try {
        versionName.split(".")[2].toInt()
    } catch (e: Exception) {
        0
    }
    private val version = Version.majorVersionOf(majorVersion)
    private val fullVersion = try {
        Version.valueOf("V_${majorVersion}_${minorVersion}").print("HQFramework- ServerVersion: ")
    } catch (e: Exception) {
        version
    }

    fun setup() {
        if (fullVersion == null)
            throw UnsupportedOperationException("unsupported version: $versionName")

        val serviceManagers = plugin.bukkitComponentRegistry.getComponents(NMSServiceManager::class)
        val serviceManager = serviceManagers.find { it.support(fullVersion) }
            ?: throw UnsupportedOperationException("unsupported version: $versionName")

        serviceManager.initialize()
        NMSServiceManager.instance = serviceManager
    }

    init {
        if (NMSServiceManager.instance == null) setup()
    }

    @Singleton
    @Bean
    override fun provideBaseComponentService(): NmsBaseComponentService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideBaseComponentService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideContainerService(): NmsContainerService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideContainerService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideArmorStandService(): NmsArmorStandService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideArmorStandService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideItemService(): NmsItemService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideItemService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideItemStackService(): NmsItemStackService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideItemStackService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideNBTTagService(): NmsNBTTagCompoundService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideNBTTagService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideVector3fService(): NmsVector3fService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideVector3fService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideWorldBorderService(): NmsWorldBorderService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideWorldBorderService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideWorldService(): NmsWorldService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideWorldService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideNettyInjectService(): NmsNettyInjectService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideNettyInjectService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideReflectionWrapperService(): NmsReflectionWrapper {
        return NMSServiceManager.instance?.getServiceProvider()?.provideReflectionWrapperService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideDisplayService(): NmsDisplayService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideDisplayService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideTextDisplayService(): NmsTextDisplayService {
        return NMSServiceManager.instance?.getServiceProvider()?.provideTextDisplayService()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideVirtualEntityClasses(): VirtualEntityClasses {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualEntityClasses()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideVirtualContainerMessageFactory(): VirtualContainerMessageFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualContainerMessageFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Qualifier("virtual.entity.camera")
    @Singleton
    @Bean
    override fun provideVirtualCameraFactory(): VirtualEntityFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualCameraFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Qualifier("virtual.entity.sign")
    @Singleton
    @Bean
    override fun provideVirtualSignFactory(): VirtualEntityFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualSignFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideVirtualAnvilHandlerFactory(): VirtualAnvilHandlerFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualAnvilHandlerFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideVirtualItemHandlerFactory(): VirtualItemHandlerFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualItemHandlerFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideVirtualSignHandlerFactory(): VirtualSignHandlerFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualSignHandlerFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideVirtualItemMessageFactory(): VirtualItemMessageFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideVirtualItemMessageFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }

    @Singleton
    @Bean
    override fun provideHQBillboardFactory(): HQBillboardFactory {
        return NMSServiceManager.instance?.getVirtualFactoryProvider()?.provideHQBillboardFactory()
            ?: throw UnsupportedOperationException("unsupported version")
    }
}