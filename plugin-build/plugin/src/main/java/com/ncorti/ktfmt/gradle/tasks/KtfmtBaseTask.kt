package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatFailure
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatSkipped
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatSuccess
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtWorkAction
import com.ncorti.ktfmt.gradle.tasks.worker.files
import com.ncorti.ktfmt.gradle.util.KtfmtResultSummary
import com.ncorti.ktfmt.gradle.util.d
import java.io.File
import java.util.UUID
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

/** ktfmt-gradle base Gradle tasks. Contains methods to properly process a single file with ktfmt */
@Suppress("LeakingThis")
abstract class KtfmtBaseTask
internal constructor(
    private val workerExecutor: WorkerExecutor,
    private val layout: ProjectLayout,
) : SourceTask() {

    init {
        includeOnly.convention("")
    }

    @get:Classpath @get:InputFiles internal abstract val ktfmtClasspath: ConfigurableFileCollection

    @get:Input internal abstract val formattingOptionsBean: Property<FormattingOptionsBean>

    @get:Option(
        option = "include-only",
        description =
            "A comma separate list of relative file paths to include exclusively. " +
                "If set the task will run the processing only on such files.",
    )
    @get:Input
    abstract val includeOnly: Property<String>

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    @IgnoreEmptyDirectories
    @SkipWhenEmpty
    override fun getSource(): FileTree = super.getSource()

    @get:OutputFile
    val output: Provider<RegularFile> = layout.buildDirectory.file("ktfmt/${this.name}/output.txt")

    @get:Internal internal abstract val reformatFiles: Boolean

    @TaskAction
    internal fun taskAction() {
        val workQueue =
            workerExecutor.processIsolation { spec -> spec.classpath.from(ktfmtClasspath) }
        execute(workQueue)
    }

    protected abstract fun execute(workQueue: WorkQueue)

    internal fun <T : KtfmtWorkAction> FileCollection.submitToQueue(
        queue: WorkQueue,
        action: Class<T>,
    ): KtfmtResultSummary {
        val workingDir = temporaryDir.resolve(UUID.randomUUID().toString())
        workingDir.mkdirs()
        try {
            val includedFiles =
                IncludedFilesParser.parse(includeOnly.get(), layout.projectDirectory.asFile)
            logger.d(
                "Preparing to format: includeOnly=${includeOnly.orNull}, includedFiles = $includedFiles"
            )
            forEach {
                queue.submit(action) { parameters ->
                    parameters.sourceFile.set(it)
                    parameters.formattingOptions.set(formattingOptionsBean.get())
                    parameters.includedFiles.set(includedFiles)
                    parameters.resultDirectory.set(workingDir)
                    parameters.reformatFiles.set(reformatFiles)
                }
            }
            queue.await()

            val results = collectResults(workingDir)

            writeResultsSummaryToOutput(results)
            return results
        } finally {
            // remove working directory and everything in it
            workingDir.deleteRecursively()
        }
    }

    private fun collectResults(tmpResultDirectory: File): KtfmtResultSummary {
        val formatResultFiles = tmpResultDirectory.listFiles() ?: emptyArray()

        val results =
            formatResultFiles
                .asSequence()
                .filter { it.isFile }
                .map { KtfmtFormatResult.parse(it.readText()) }

        val correctFiles =
            results.filter { it is KtfmtFormatSuccess && it.wasCorrectlyFormatted }.files()
        val incorrectFiles =
            results.filter { it is KtfmtFormatSuccess && !it.wasCorrectlyFormatted }.files()
        val skippedFiles = results.filter { it is KtfmtFormatSkipped }.files()
        val failedFiles = results.filter { it is KtfmtFormatFailure }.files()

        return KtfmtResultSummary(correctFiles, incorrectFiles, skippedFiles, failedFiles)
    }

    private fun writeResultsSummaryToOutput(results: KtfmtResultSummary) =
        output.get().asFile.writeText(results.prettyPrint())
}
