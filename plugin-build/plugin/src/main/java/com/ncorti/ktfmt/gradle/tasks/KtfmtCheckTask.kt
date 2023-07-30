package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.d
import com.ncorti.ktfmt.gradle.util.i
import org.gradle.api.file.ProjectLayout
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/** ktfmt-gradle Check task. Verifies if the output of ktfmt is the same as the input */
abstract class KtfmtCheckTask @Inject constructor(
    workerExecutor: WorkerExecutor,
    layout: ProjectLayout
) : KtfmtBaseTask(workerExecutor, layout) {

    init {
        group = KtfmtUtils.GROUP_VERIFICATION
    }

    override fun execute(workQueue: WorkQueue) {
        val results = inputFiles.submitToQueue(workQueue, KtfmtCheckAction::class.java)

        logger.d("Format results: $results")
        val failures = results.filterIsInstance<Result.Failure>()

        if (failures.isNotEmpty()) {
            error("Ktfmt failed to run with ${failures.size} failures")
        }

        val notFormattedFiles =
            results.filterIsInstance<Result.Success>().filterNot { it.correctlyFormatted }

        if (notFormattedFiles.isNotEmpty()) {
            val fileList = notFormattedFiles.joinToString("\n") { it.relativePath }
            error(
                "[ktfmt] Found ${notFormattedFiles.size} files that are not properly formatted:\n$fileList"
            )
        }

        logger.i("Successfully checked ${results.size} files with Ktfmt")
    }
}
