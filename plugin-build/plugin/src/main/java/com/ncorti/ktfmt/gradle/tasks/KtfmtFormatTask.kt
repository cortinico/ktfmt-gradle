package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtWorkAction
import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.i
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

/** ktfmt-gradle Format task. Replaces input file content with its formatted equivalent. */
abstract class KtfmtFormatTask
@Inject
internal constructor(workerExecutor: WorkerExecutor, layout: ProjectLayout) :
    KtfmtBaseTask(workerExecutor, layout) {

    init {
        group = KtfmtUtils.GROUP_FORMATTING
    }

    override val reformatFiles: Boolean
        get() = true

    override fun execute(workQueue: WorkQueue) {
        val resultSummary = source.submitToQueue(workQueue, KtfmtWorkAction::class.java)

        if (resultSummary.failedFiles.isNotEmpty()) {
            error("Ktfmt failed to run with ${resultSummary.failedFiles.size} failures")
        }

        if (resultSummary.invalidFormattedFiles.isNotEmpty()) {
            val countInvalidFiles = resultSummary.invalidFormattedFiles.size
            logger.i("Successfully reformatted $countInvalidFiles files with Ktfmt")
            return
        }

        val countValidFiles = resultSummary.validFormattedFiles.size
        logger.i("All files ($countValidFiles) are already correctly formatted")
    }
}
