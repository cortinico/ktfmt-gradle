package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.d
import com.ncorti.ktfmt.gradle.util.i
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.OutputFiles
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/** ktfmt-gradle Format task. Replaces input file content with its formatted equivalent. */
abstract class KtfmtFormatTask @Inject constructor(
    workerExecutor: WorkerExecutor,
    layout: ProjectLayout
) : KtfmtBaseTask(workerExecutor, layout) {

    @get:OutputFiles
    protected val outputFiles: FileCollection
        get() = inputFiles

    init {
        group = KtfmtUtils.GROUP_FORMATTING
    }

    override fun execute(workQueue: WorkQueue) {
        val results = inputFiles.submitToQueue(workQueue, KtfmtFormatAction::class.java)

        logger.d("Format results: $results")
        val failures = results.filterIsInstance<Result.Failure>()

        if (failures.isNotEmpty()) {
            error("Ktfmt failed to run with ${failures.size} failures")
        }

        val notFormattedFiles =
            results.filterIsInstance<Result.Success>().filterNot { it.correctlyFormatted }

        logger.i("Successfully reformatted ${notFormattedFiles.count()} files with Ktfmt")
    }
}
