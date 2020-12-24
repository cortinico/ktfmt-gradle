package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.e
import com.ncorti.ktfmt.gradle.util.i
import java.nio.charset.Charset

/** ktfmt-gradle Format task. Replaces input file content with its formatted equivalent. */
abstract class KtfmtFormatTask : KtfmtBaseTask() {

    init {
        group = KtfmtUtils.GROUP_FORMATTING
    }

    override suspend fun execute() {
        val result = processFileCollection(inputFiles)
        result.forEach {
            when {
                it is KtfmtSuccess && it.isCorrectlyFormatted ->
                    logger.i("Valid formatting for: ${it.input}")
                it is KtfmtSuccess && !it.isCorrectlyFormatted -> {
                    logger.i("Reformatting...: ${it.input}")
                    it.input.writeText(it.formattedCode, Charset.defaultCharset())
                }
                it is KtfmtFailure ->
                    logger.e("Failing to format: ${it.input}\nError: ${it.message}")
            }
        }

        val failures = result.filterIsInstance<KtfmtFailure>()

        if (failures.isNotEmpty()) {
            error("Ktfmt failed to run with ${failures.size} failures")
        }

        val notFormattedFiles =
            result.filterIsInstance<KtfmtSuccess>().filterNot(KtfmtSuccess::isCorrectlyFormatted)

        logger.i("Successfully reformatted ${notFormattedFiles.count()} files with Ktfmt")
    }
}
