package com.ncorti.ktfmt.gradle.tasks

import com.facebook.ktfmt.format.Formatter.format
import com.facebook.ktfmt.format.ParseError
import com.google.common.annotations.VisibleForTesting
import com.google.googlejavaformat.FormattingError
import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import com.ncorti.ktfmt.gradle.KtfmtExtension
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * ktfmt-gradle base Gradle tasks. Handles a coroutine scope and contains method to propertly
 * process a single file with ktfmt
 */
abstract class KtfmtBaseTask : SourceTask() {

    @get:Input @get:Optional internal var bean: FormattingOptionsBean? = null

    @get:Option(
        option = "include-only",
        description =
            "A comma separate list of relative file paths to include exclusively. " +
                "If set the task will run the processing only on such files."
    )
    @get:Input
    open val includeOnly: Property<String> =
        project.objects.property(String::class.java).convention("")

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    @get:IgnoreEmptyDirectories
    protected val inputFiles: FileCollection
        get() = super.getSource()

    @TaskAction
    @VisibleForTesting
    internal fun taskAction() {
        runBlocking(Dispatchers.IO) { execute() }
    }

    protected abstract suspend fun execute()

    @VisibleForTesting
    @Suppress("TooGenericExceptionCaught", "SpreadOperator")
    internal fun processFile(input: File): KtfmtResult {
        // The task should try to read from the ktfmt{} extension
        // before falling back to default values.
        if (bean == null) {
            bean =
                try {
                    project.extensions.getByType(KtfmtExtension::class.java).toBean()
                } catch (ignored: UnknownDomainObjectException) {
                    FormattingOptionsBean()
                }
        }

        if (includeOnly.orNull?.isNotEmpty() == true) {
            val includeOnlyPaths =
                includeOnly
                    .get()
                    .split(',', ':')
                    .map {
                        Paths.get(
                            "",
                            *it.trim().split(File.separatorChar, '\\', '/').toTypedArray()
                        )
                    }
                    .toSet()
            val relativePath = input.relativeTo(project.projectDir).toPath()
            if (relativePath !in includeOnlyPaths) {
                return KtfmtSkipped(input, "Not included inside --include-only")
            }
        }
        return try {
            val originCode = input.readText()
            val formattedCode = format(bean!!.toFormattingOptions(), originCode)
            val isCorrectlyFormatted = originCode == formattedCode
            KtfmtSuccess(input, isCorrectlyFormatted, formattedCode)
        } catch (cause: Throwable) {
            val message =
                when (cause) {
                    is IOException -> "Unable to read file"
                    is ParseError -> "Failed to parse file"
                    is FormattingError -> cause.diagnostics().joinToString("\n") { "$input:$it" }
                    else -> "Generic error during file processing"
                }
            KtfmtFailure(input, message, cause)
        }
    }

    internal suspend fun processFileCollection(input: FileCollection): List<KtfmtResult> =
        coroutineScope {
            input.map { async { processFile(it) } }.awaitAll()
        }
}
