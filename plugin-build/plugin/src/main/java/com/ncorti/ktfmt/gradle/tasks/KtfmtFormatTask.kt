package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.KtfmtUtils
import com.ncorti.ktfmt.gradle.util.e
import com.ncorti.ktfmt.gradle.util.i
import java.nio.charset.Charset
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputFiles

/** ktfmt-gradle Format task. Replaces input file content with its formatted equivalent. */
abstract class KtfmtFormatTask : KtfmtBaseTask() {

    @get:OutputFiles
    protected val outputFiles: FileCollection
        get() = inputFiles

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

        logger.i("Successfully reformatted ${notFormattedFiles.count()} files with Ktfmt")
    }
}
