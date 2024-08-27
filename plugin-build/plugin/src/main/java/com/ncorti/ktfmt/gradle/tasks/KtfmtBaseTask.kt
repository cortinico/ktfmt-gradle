package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtWorkAction
import com.ncorti.ktfmt.gradle.tasks.worker.Result
import com.ncorti.ktfmt.gradle.util.d
import java.util.UUID
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
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
    ): List<Result> {
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
                    parameters.projectDir.set(layout.projectDirectory)
                    parameters.workingDir.set(workingDir)
                }
            }
            queue.await()

            val files = workingDir.listFiles() ?: emptyArray()
            return files
                .asSequence()
                .filter { it.isFile }
                .map { Result.fromResultString(it.readText()) }
                .toList()
        } finally {
            // remove working directory and everything in it
            workingDir.deleteRecursively()
        }
    }
}
