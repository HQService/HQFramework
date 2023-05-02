package bukkitResourceGenerator

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer
import com.fasterxml.jackson.databind.util.Converter
import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class BukkitResourceGeneratorTask : DefaultTask() {
    @Input
    val fileName: Property<String> = project.objects.property()

    @Nested
    val pluginResource: Property<BukkitResourceGeneratorProperties> = project.objects.property()

    @OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @Suppress("UNCHECKED_CAST")
    @TaskAction
    fun generate() {
        val factory = YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS)

        val module = SimpleModule()

        module.addSerializer(
            StdDelegatingSerializer(
                NamedDomainObjectCollection::class.java,
                NamedDomainObjectCollectionConverter as Converter<NamedDomainObjectCollection<*>, *>
            )
        )

        val mapper = ObjectMapper(factory)
            .registerKotlinModule()
            .registerModule(module)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

        mapper.writeValue(outputDirectory.file(fileName).get().asFile, pluginResource.get())
    }

    object NamedDomainObjectCollectionConverter : StdConverter<NamedDomainObjectCollection<Any>, Map<String, Any>>() {
        override fun convert(value: NamedDomainObjectCollection<Any>): Map<String, Any> {
            val namer = value.namer
            return value.associateBy { namer.determineName(it) }
        }
    }
}