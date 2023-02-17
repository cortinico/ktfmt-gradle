package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.KtfmtDiffer.computeDiff
import com.ncorti.ktfmt.gradle.util.KtfmtDiffer.printDiff
import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.e
import com.ncorti.ktfmt.gradle.util.i

/** ktfmt-gradle Check task. Verifies if the output of ktfmt is the same as the input */
abstract class KtfmtCheckTask : KtfmtBaseTask() {

    init {
        group = KtfmtUtils.GROUP_VERIFICATION
    }

    override suspend fun execute() {
        val result = processFileCollection(inputFiles)
        result.forEach {
            when {
                it is KtfmtSuccess && it.isCorrectlyFormatted ->
                    logger.i("Valid formatting for: ${it.input}")
                it is KtfmtSuccess && !it.isCorrectlyFormatted -> {
                    logger.e("Invalid formatting for: ${it.input}")
                    printDiff(computeDiff(it), logger)
                }
                it is KtfmtSkipped -> logger.i("Skipping for: ${it.input} because: ${it.reason}")
                it is KtfmtFailure -> {
                    logger.e("Failed to analyse: ${it.input}")
                    it.message.split("\n").forEach { line -> logger.e("e: $line") }
                }
            }
        }

        val failures = result.filterIsInstance<KtfmtFailure>()

        if (failures.isNotEmpty()) {
            error("Ktfmt failed to run with ${failures.size} failures")
        }

        val notFormattedFiles =
            result.filterIsInstance<KtfmtSuccess>().filterNot(KtfmtSuccess::isCorrectlyFormatted)

        if (notFormattedFiles.isNotEmpty()) {
            val fileList = notFormattedFiles.map { it.input }.joinToString("\n")
            error(
                "[ktfmt] Found ${notFormattedFiles.size} files that are not properly formatted:\n$fileList"
            )
        }

        logger.i("Successfully checked ${result.count()} files with Ktfmt")
    }
}
