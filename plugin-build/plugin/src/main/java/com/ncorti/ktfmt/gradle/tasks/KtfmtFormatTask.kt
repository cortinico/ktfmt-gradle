package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtWorkAction
import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.d
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
        // Since the input files are also the output files, we do not have to specify outputs again
        outputs.upToDateWhen { true }
    }

    override val reformatFiles: Boolean
        get() = true

    override fun execute(workQueue: WorkQueue) {
        val results = source.submitToQueue(workQueue, KtfmtWorkAction::class.java)

        logger.d("Format results: $results")
        val failures = results.filterIsInstance<KtfmtFormatResult.KtfmtFormatFailure>()

        if (failures.isNotEmpty()) {
            error("Ktfmt failed to run with ${failures.size} failures")
        }

        val notFormattedFiles =
            results.filterIsInstance<KtfmtFormatResult.KtfmtFormatSuccess>().filterNot {
                it.wasCorrectlyFormatted
            }

        logger.i("Successfully reformatted ${notFormattedFiles.count()} files with Ktfmt")
    }
}
