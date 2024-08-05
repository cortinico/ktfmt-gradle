package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtCheckAction
import com.ncorti.ktfmt.gradle.tasks.worker.Result
import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.d
import com.ncorti.ktfmt.gradle.util.i
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

/** ktfmt-gradle Check task. Verifies if the output of ktfmt is the same as the input */
@CacheableTask
abstract class KtfmtCheckTask
@Inject
internal constructor(workerExecutor: WorkerExecutor, layout: ProjectLayout) :
    KtfmtBaseTask(workerExecutor, layout) {

    @get:OutputFile
    val output: Provider<RegularFile> = layout.buildDirectory.file("ktfmt/${this.name}/output.txt")

    init {
        group = KtfmtUtils.GROUP_VERIFICATION
    }

    override fun execute(workQueue: WorkQueue) {
        val results = source.submitToQueue(workQueue, KtfmtCheckAction::class.java)

        logger.d("Check results: $results")
        val failures = results.filterIsInstance<Result.Failure>()

        if (failures.isNotEmpty()) {
            error("Ktfmt failed to run with ${failures.size} failures")
        }

        val notFormattedFiles =
            results.filterIsInstance<Result.Success>().filterNot { it.correctlyFormatted }

        if (notFormattedFiles.isNotEmpty()) {
            val fileList = notFormattedFiles.joinToString("\n") { it.relativePath }
            error(
                "[ktfmt] Found ${notFormattedFiles.size} files that are not properly formatted:\n$fileList")
        }

        val message = "Successfully checked ${results.size} files with Ktfmt"
        logger.i(message)
        output.get().asFile.writeText(message)
    }
}
