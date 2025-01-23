package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtWorkAction
import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.i
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.CacheableTask
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

/** ktfmt-gradle Check task. Verifies if the output of ktfmt is the same as the input */
@CacheableTask
abstract class KtfmtCheckTask
@Inject
internal constructor(workerExecutor: WorkerExecutor, private val layout: ProjectLayout) :
    KtfmtBaseTask(workerExecutor, layout) {

    init {
        group = KtfmtUtils.GROUP_VERIFICATION
    }

    override val reformatFiles: Boolean
        get() = false

    override fun execute(workQueue: WorkQueue) {
        val resultSummary = source.submitToQueue(workQueue, KtfmtWorkAction::class.java)

        if (resultSummary.failedFiles.isNotEmpty()) {
            error("Ktfmt failed to run with ${resultSummary.failedFiles.size} failures")
        }

        if (resultSummary.invalidFormattedFiles.isNotEmpty()) {
            val fileList =
                resultSummary.invalidFormattedFiles.joinToString("\n") {
                    it.relativeTo(layout.projectDirectory.asFile).path
                }
            val invalidFilesSize = resultSummary.invalidFormattedFiles.size
            error(
                "[ktfmt] Found $invalidFilesSize files that are not properly formatted:\n$fileList"
            )
        }

        logger.i("Successfully checked ${resultSummary.validFormattedFiles.size} files with Ktfmt")
    }
}
