package kr.hqservice.framework.bukkit.core

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.component.registry.registry.BukkitComponentRegistry
import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.AttachableExceptionHandler
import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.ExceptionHandlerRegistry
import kr.hqservice.framework.bukkit.core.coroutine.component.exceptionhandler.HandleResult
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.extension.format
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.util.AnsiColor
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.extension.yaml
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

abstract class HQBukkitPlugin : JavaPlugin, HQPlugin, KoinComponent, CoroutineScope, ExceptionHandlerRegistry {
    constructor() : super()
    internal constructor(
        loader: JavaPluginLoader,
        description: PluginDescriptionFile,
        dataFolder: File,
        file: File
    ) : super(loader, description, dataFolder, file)

    protected open val componentRegistry: BukkitComponentRegistry by inject { parametersOf(this) }
    private val config = File(dataFolder, "config.yml").yaml()

    internal companion object GlobalExceptionHandlerRegistry : ExceptionHandlerRegistry {
        private val exceptionHandlers: MutableList<AttachableExceptionHandler> = mutableListOf()

        override fun attachExceptionHandler(attachableExceptionHandler: AttachableExceptionHandler) {
            exceptionHandlers.add(attachableExceptionHandler)
            exceptionHandlers.sortBy { attachableExceptionHandler.priority }
        }
    }

    private val exceptionHandlers: MutableList<AttachableExceptionHandler> = mutableListOf()
    private val supervisorJob = SupervisorJob()
    private val pluginCoroutineContextElement get() = PluginCoroutineContextElement(this)
    private val coroutineExceptionHandler = CoroutineExceptionHandler handler@{ coroutineContext, throwable ->
        val exceptionHandlers = listOf(
            *GlobalExceptionHandlerRegistry.exceptionHandlers.toTypedArray(),
            *this.exceptionHandlers.toTypedArray()
        )
        exceptionHandlers.forEach forEach@{ handler ->
            when (handler.handle(throwable)) {
                HandleResult.HANDLED -> return@handler
                HandleResult.HANDLED_MUST_STORE -> {
                    storeStackTrace(throwable)
                    return@handler
                }

                HandleResult.UNHANDLED -> return@forEach
            }
        }
        logUnhandledExceptionInfo(throwable)
        storeStackTrace(throwable)
    }

    private fun logUnhandledExceptionInfo(throwable: Throwable) {
        with(logger) {
            severe("an unhandled exception occurs in CoroutineContext named ${coroutineContext[CoroutineName]?.name ?: "UNNAMED"}")
            severe("exception: ${throwable::class.simpleName}")
            severe("cause: ${throwable.cause}")
            severe("message: ${throwable.message}")
        }
    }

    private fun storeStackTrace(throwable: Throwable) {
        val nowString = LocalDateTime.now().format("yyyyMMdd_HHmmssSSS")
        val fileName = nowString + "_" + this.name + "_" + throwable::class.simpleName + "_" + ".txt"
        val folder = getErrorFolder()
        if (!folder.exists()) folder.mkdir()
        PrintWriter(File(folder, fileName)).use { printWriter ->
            throwable.printStackTrace(printWriter)
            printWriter.println()
        }
        val limit = getErrorLimit()
        if (limit > 0) {
            try {
                Files.list(folder.toPath()).use { errorPaths ->
                    errorPaths
                        .map { path -> path.toFile() }
                        .sorted(
                            Comparator.comparing(
                                { file -> file.lastModified() },
                                Comparator.reverseOrder()
                            )
                        )
                        .skip(limit.toLong())
                        .forEach { file -> file.delete() }
                }
            } catch (exception: Exception) {
                logger.severe("an error occurs while cleaning up error folder")
                exception.printStackTrace()
            }
        }
    }

    private fun getErrorLimit(): Int {
        return server.pluginManager.getPlugin("HQFramework")!!.config.getInt("log.error.store-limit", 1000)
    }

    private fun getErrorFolder(): File {
        return File(server.pluginManager.getPlugin("HQFramework")!!.config.getString("log.error.path", "hq-errors/")!!)
    }

    override fun attachExceptionHandler(attachableExceptionHandler: AttachableExceptionHandler) {
        exceptionHandlers.add(attachableExceptionHandler)
        exceptionHandlers.sortBy { attachableExceptionHandler.priority }
    }

    override val coroutineContext: CoroutineContext
        get() = CoroutineName("${this.name}CoroutineScope") + Dispatchers.BukkitMain + coroutineExceptionHandler + supervisorJob + pluginCoroutineContextElement

    open val group = "HQPlugin"

    fun getHQConfig(): HQYamlConfiguration {
        return config
    }

    final override fun onLoad() {
        onPreLoad()
        onPostLoad()
    }

    final override fun onEnable() {
        runBlocking {
            launch(Dispatchers.Unconfined){
                val timerJob = launch(Dispatchers.Default) timer@{
                    var index = 0
                    while (this@timer.isActive) {
                        index++
                        logger.info("${AnsiColor.CYAN}Enabling${".".repeat(index)}${AnsiColor.RESET}")
                        delay(1000)
                    }
                }
                onPreEnable()
                val folder = getErrorFolder()
                if (!folder.exists()) folder.mkdir()
                loadConfigIfExist()
                componentRegistry.setup()
                onPostEnable()
                timerJob.cancel()
                logger.info("${AnsiColor.CYAN}${this@HQBukkitPlugin.name} initialized successfully and is ready for service.${AnsiColor.RESET}")
            }.join()
        }
    }

    final override fun onDisable() {
        runBlocking {
            launch(Dispatchers.Unconfined) {
                logger.info("${AnsiColor.CYAN}Disabling...${AnsiColor.RESET}")
                onPreDisable()
                componentRegistry.teardown()
                supervisorJob.children.toList().joinAll()
                onPostDisable()
                logger.info("${AnsiColor.CYAN}Teardown finished.${AnsiColor.RESET}")
            }.join()
        }
    }

    fun reload() {
        onDisable()
        onEnable()
    }

    final override fun getJar(): File {
        return super.getFile()
    }

    final override fun getLogger(): Logger {
        return super.getLogger()
    }

    final override fun getPluginClassLoader(): ClassLoader {
        return super.getClassLoader()
    }

    private fun loadConfigIfExist() {
        val stream = getResource("config.yml") ?: return
        val file = File(dataFolder, "config.yml")
        if (!dataFolder.exists()) dataFolder.mkdirs()
        if (!file.exists()) file.bufferedWriter().use { writer ->
            stream.reader().readLines().forEach {
                writer.appendLine(it)
            }
        }
    }
}