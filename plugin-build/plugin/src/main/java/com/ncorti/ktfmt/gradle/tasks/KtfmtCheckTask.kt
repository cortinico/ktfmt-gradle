package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.KtfmtResultSummary
import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.i
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.CacheableTask

/** ktfmt-gradle Check task. Verifies if the output of ktfmt is the same as the input */
@CacheableTask
abstract class KtfmtCheckTask @Inject internal constructor(private val layout: ProjectLayout) :
    KtfmtBaseTask(layout) {

    init {
        group = KtfmtUtils.GROUP_VERIFICATION
    }

    override val reformatFiles: Boolean
        get() = false

    override fun handleResultSummary(resultSummary: KtfmtResultSummary) {
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
