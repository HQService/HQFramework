package kr.hqservice.framework.bukkit.core

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.component.registry.BukkitComponentRegistry
import kr.hqservice.framework.bukkit.core.coroutine.component.AttachableExceptionHandler
import kr.hqservice.framework.bukkit.core.coroutine.component.ExceptionHandlerRegistry
import kr.hqservice.framework.bukkit.core.coroutine.component.HandleResult
import kr.hqservice.framework.bukkit.core.coroutine.element.PluginCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.extension.format
import kr.hqservice.framework.global.core.HQPlugin
import kr.hqservice.framework.global.core.component.error.IllegalDependException
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
        val exceptionHandlers = listOf(*GlobalExceptionHandlerRegistry.exceptionHandlers.toTypedArray(), *this.exceptionHandlers.toTypedArray())
        exceptionHandlers.forEach forEach@{ handler ->
            when (handler.handle(throwable)) {
                HandleResult.HANDLED -> return@handler
                HandleResult.MUST_STORE -> {
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
        val folder = File("hq-errors")
        if (!folder.exists()) folder.mkdir()
        PrintWriter(File(folder, fileName)).use { printWriter ->
            var cause: Throwable? = throwable
            while (cause != null) {
                throwable.printStackTrace(printWriter)
                printWriter.println()
                cause = throwable.cause
            }
        }
        val limit = config.getInt("error-log-limit", 100)
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

    override fun attachExceptionHandler(attachableExceptionHandler: AttachableExceptionHandler) {
        exceptionHandlers.add(attachableExceptionHandler)
        exceptionHandlers.sortBy { attachableExceptionHandler.priority }
    }

    override val coroutineContext: CoroutineContext
        get() = CoroutineName("${this.name}CoroutineScope") + Dispatchers.BukkitMain + coroutineExceptionHandler + supervisorJob + pluginCoroutineContextElement

    open val group = "HQPlugin"

    final override fun onLoad() {
        onPreLoad()
        onPostLoad()
    }

    final override fun onEnable() {
        onPreEnable()
        loadConfigIfExist()
        componentRegistry.setup()
        onPostEnable()

        launch {
            throw IllegalDependException(listOf())
        }
        launch {
            throw RuntimeException("runtime~@!")
        }
    }

    final override fun onDisable() {
        onPreDisable()
        componentRegistry.teardown()
        runBlocking {
            supervisorJob.children.toList().joinAll()
        }
        onPostDisable()
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