package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import com.ncorti.ktfmt.gradle.KtfmtExtension
import com.ncorti.ktfmt.gradle.KtfmtPlugin
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtWorkAction
import com.ncorti.ktfmt.gradle.tasks.worker.Result
import com.ncorti.ktfmt.gradle.util.d
import java.io.File
import java.util.*
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

/**
 * ktfmt-gradle base Gradle tasks. Handles a coroutine scope and contains method to propertly
 * process a single file with ktfmt
 */
@Suppress("LeakingThis")
abstract class KtfmtBaseTask
internal constructor(
    private val workerExecutor: WorkerExecutor,
    private val layout: ProjectLayout
) : SourceTask() {

    init {
        includeOnly.convention("")
    }

    @get:Classpath @get:InputFiles internal abstract val ktfmtClasspath: ConfigurableFileCollection

    @get:Input @get:Optional internal var bean: FormattingOptionsBean? = null

    @get:Option(
        option = "include-only",
        description =
            "A comma separate list of relative file paths to include exclusively. " +
                "If set the task will run the processing only on such files."
    )
    @get:Input
    abstract val includeOnly: Property<String>

    @get:Option(
        option = "isolation-strategy",
        description =
            "Possible isolation strategies for invoking ktfmt. Can be PROCESS, CLASS_LOADER, NO_ISOLATION. Default is NO_ISOLATION"
    )
    @get:Optional
    @get:Input
    abstract val isolationStrategy: Property<String>

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    @get:IgnoreEmptyDirectories
    internal val inputFiles: FileCollection
        get() = super.getSource().filter(defaultExcludesFilter)

    @TaskAction
    internal fun taskAction() {
        val workQueue = getWorkerQueue()

        val startTime = System.currentTimeMillis()
        execute(workQueue)
        val endTime = System.currentTimeMillis()

        logger.info("Worker queue finished after ${endTime-startTime} ms")
    }

    private fun getWorkerQueue(): WorkQueue {
        val isolationStrategy = isolationStrategy.getOrElse("NO_ISOLATION")
        logger.info("Selected workerQueue isolation strategy is $isolationStrategy")
        return when (isolationStrategy) {
            "PROCESS" ->
                workerExecutor.processIsolation() { spec -> spec.classpath.from(ktfmtClasspath) }
            "CLASS_LOADER" ->
                workerExecutor.classLoaderIsolation() { spec ->
                    spec.classpath.from(ktfmtClasspath)
                }
            "NO_ISOLATION" -> workerExecutor.noIsolation()
            else -> workerExecutor.noIsolation()
        }
    }

    protected abstract fun execute(workQueue: WorkQueue)

    private fun formattingOptions(): FormattingOptionsBean {
        if (bean == null) {
            // TODO - set this as property / convention
            bean =
                try {
                    project.extensions.getByType(KtfmtExtension::class.java).toBean()
                } catch (ignored: UnknownDomainObjectException) {
                    FormattingOptionsBean()
                }
        }
        return bean!!
    }

    @get:Internal
    internal val defaultExcludesFilter: Spec<File> =
        Spec<File> {
            if (this.excludes.containsAll(KtfmtPlugin.defaultExcludes) && this.excludes.size == 1) {
                it.absolutePath.matches(KtfmtPlugin.defaultExcludesRegex).not()
            } else {
                true
            }
        }

    internal fun <T : KtfmtWorkAction> FileCollection.submitToQueue(
        queue: WorkQueue,
        action: Class<T>
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
                    parameters.formattingOptions.set(formattingOptions())
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
