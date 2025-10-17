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
import javax.inject.Inject
import org.gradle.api.file.ConfigurableFileCollection
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
import org.gradle.workers.WorkerExecutor

/** ktfmt-gradle base Gradle tasks. Contains methods to properly process a single file with ktfmt */
@Suppress("LeakingThis")
public abstract class KtfmtBaseTask(private val layout: ProjectLayout) : SourceTask() {

    init {
        includeOnly.convention("")
    }

    @get:Classpath @get:InputFiles public abstract val ktfmtClasspath: ConfigurableFileCollection

    @get:Input public abstract val formattingOptionsBean: Property<FormattingOptionsBean>

    @get:Option(
        option = "include-only",
        description =
            "A comma separate list of relative file paths to include exclusively. " +
                "If set the task will run the processing only on such files.",
    )
    @get:Input
    public abstract val includeOnly: Property<String>

    @get:Input public abstract val useClassloaderIsolation: Property<Boolean>

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    @IgnoreEmptyDirectories
    @SkipWhenEmpty
    override fun getSource(): FileTree = super.getSource()

    @get:OutputFile
    public val output: Provider<RegularFile> =
        layout.buildDirectory.file("ktfmt/${this.name}/output.txt")

    @get:Inject internal abstract val workerExecutor: WorkerExecutor

    @get:Internal public abstract val reformatFiles: Boolean

    /**
     * Called after all files have been analyzed and [resultSummary] has been written into [output].
     */
    protected abstract fun handleResultSummary(resultSummary: KtfmtResultSummary)

    @TaskAction
    internal fun taskAction() {
        val tmpResultDirectory =
            temporaryDir.resolve(UUID.randomUUID().toString()).apply { mkdirs() }

        try {
            submitFilesToFormatterWorker(tmpResultDirectory)

            val results = collectResults(tmpResultDirectory)

            reportFailedFiles(results)
            writeResultsSummaryToOutput(results)
            handleResultSummary(results)
        } finally {
            tmpResultDirectory.deleteRecursively()
        }
    }

    private fun submitFilesToFormatterWorker(tmpResultDirectory: File) {
        val queue =
            if (useClassloaderIsolation.get()) {
                workerExecutor.classLoaderIsolation { it.classpath.from(ktfmtClasspath) }
            } else {
                workerExecutor.processIsolation { it.classpath.from(ktfmtClasspath) }
            }

        val includedFiles = getIncludedFiles()

        source.forEach { file ->
            queue.submit(KtfmtWorkAction::class.java) {
                it.sourceFile.set(file)
                it.formattingOptions.set(formattingOptionsBean.get())
                it.includedFiles.set(includedFiles)
                it.resultDirectory.set(tmpResultDirectory)
                it.reformatFiles.set(reformatFiles)
            }
        }

        queue.await()
    }

    private fun getIncludedFiles(): Set<File> {
        val includedFiles =
            IncludedFilesParser.parse(includeOnly.get(), layout.projectDirectory.asFile)
        logger.d(
            "Preparing to format: includeOnly=${includeOnly.orNull}, includedFiles = $includedFiles"
        )
        return includedFiles
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

    private fun reportFailedFiles(resultSummary: KtfmtResultSummary) {
        if (resultSummary.failedFiles.isNotEmpty()) {
            val fileList =
                resultSummary.failedFiles.joinToString("\n") {
                    it.relativeTo(layout.projectDirectory.asFile).path
                }

            error("Ktfmt failed to run with ${resultSummary.failedFiles.size} failures:\n$fileList")
        }
    }

    private fun writeResultsSummaryToOutput(results: KtfmtResultSummary) =
        output.get().asFile.writeText(results.prettyPrint())
}
