package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.KtfmtResultSummary
import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.i
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout

/** ktfmt-gradle Format task. Replaces input file content with its formatted equivalent. */
public abstract class KtfmtFormatTask @Inject public constructor(layout: ProjectLayout) :
    KtfmtBaseTask(layout) {

    init {
        group = KtfmtUtils.GROUP_FORMATTING
    }

    final override val reformatFiles: Boolean = true

    override fun handleResultSummary(resultSummary: KtfmtResultSummary) {
        if (resultSummary.invalidFormattedFiles.isNotEmpty()) {
            val countInvalidFiles = resultSummary.invalidFormattedFiles.size
            logger.i("Successfully reformatted $countInvalidFiles files with Ktfmt")
            return
        }

        val countValidFiles = resultSummary.validFormattedFiles.size
        logger.i("All files ($countValidFiles) are already correctly formatted")
    }
}
