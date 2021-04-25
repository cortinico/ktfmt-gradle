package com.ncorti.ktfmt.gradle.tasks

import com.facebook.ktfmt.ParseError
import com.facebook.ktfmt.format
import com.google.common.annotations.VisibleForTesting
import com.google.googlejavaformat.FormattingError
import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

/**
 * ktfmt-gradle base Gradle tasks. Handles a coroutine scope and contains method to propertly
 * process a single file with ktfmt
 */
abstract class KtfmtBaseTask : SourceTask() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @get:Input internal var bean = FormattingOptionsBean()

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

    @get:OutputFiles
    protected val outputFiles: FileCollection
        get() = inputFiles

    @TaskAction
    @VisibleForTesting
    internal fun taskAction() {
        runBlocking(scope.coroutineContext) { execute() }
        scope.cancel()
    }

    protected abstract suspend fun execute()

    @VisibleForTesting
    @Suppress("TooGenericExceptionCaught")
    internal fun processFile(input: File): KtfmtResult {
        if (includeOnly.orNull?.isNotEmpty() == true) {
            val includeOnlyPaths =
                includeOnly.get().split(',', ':').map(String::trim).map {
                    handleWindowsFileSeparator(it)
                }
            val relativePath = input.relativeTo(project.projectDir).toString()
            if (relativePath !in includeOnlyPaths) {
                return KtfmtSkipped(input, "Not included inside --include-only")
            }
        }
        return try {
            val originCode = input.readText()
            val formattedCode = format(bean.toFormattingOptions(), originCode)
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

    private fun handleWindowsFileSeparator(path: String): String {
        if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
            return path.replace('/', '\\')
        }
        return path
    }

    internal suspend fun processFileCollection(input: FileCollection): List<KtfmtResult> {
        return input.map { scope.async { processFile(it) } }.awaitAll()
    }

    @VisibleForTesting @Internal internal fun isScopeActive() = scope.isActive
}
