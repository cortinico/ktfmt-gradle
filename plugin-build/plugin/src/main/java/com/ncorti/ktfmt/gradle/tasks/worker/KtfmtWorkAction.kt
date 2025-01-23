package com.ncorti.ktfmt.gradle.tasks.worker

import com.facebook.ktfmt.format.Formatter
import com.facebook.ktfmt.format.FormattingOptions
import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatFailure
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatSkipped
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatSuccess
import com.ncorti.ktfmt.gradle.util.KtfmtDiffer
import com.ncorti.ktfmt.gradle.util.d
import com.ncorti.ktfmt.gradle.util.e
import com.ncorti.ktfmt.gradle.util.i
import java.io.File
import java.nio.charset.Charset
import java.util.UUID
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Gradle WorkAction to format a single Kotlin file using ktfmt.
 *
 * This class is the ONLY class that should use classes from ktfmt (`com.facebook.ktfmt`) to
 * maintain classloader isolation.
 *
 * ktfmt is a `compileOnly` dependency which will result in ClassNotFound exceptions should it be
 * used outside of this WorkAction.
 *
 * This WorkAction has a specific, isolated classloader whose classpath contains `ktfmt` (and any
 * transitive dependencies)
 */
internal abstract class KtfmtWorkAction : WorkAction<KtfmtWorkAction.KtfmtWorkParameters> {
    protected val logger: Logger = Logging.getLogger(KtfmtWorkAction::class.java)

    interface KtfmtWorkParameters : WorkParameters {
        val sourceFile: RegularFileProperty
        val formattingOptions: Property<FormattingOptionsBean>
        val reformatFiles: Property<Boolean>
        val includedFiles: SetProperty<File>
        val resultDirectory: DirectoryProperty
    }

    final override fun execute() {
        val result = processSourceFile(parameters)

        writeResult(result)
    }

    private fun processSourceFile(parameter: KtfmtWorkParameters): KtfmtFormatResult {
        val sourceFile = parameter.sourceFile.get().asFile
        val reformatFiles = parameter.reformatFiles.get()
        val includedFiles = parameter.includedFiles.get()
        val formattingOptions = parameter.formattingOptions.get().toFormattingOptions()

        if (shouldSkipFile(sourceFile, includedFiles)) {
            logger.i("Skipping format for $sourceFile because it is not included")
            return KtfmtFormatSkipped(sourceFile)
        }

        logger.d("Checking format for $sourceFile")

        return runCatching {
                val originalContent = sourceFile.readText()

                val formattedContent = Formatter.format(formattingOptions, originalContent)

                if (originalContent == formattedContent) {
                    logger.i("Valid formatting for: $sourceFile")
                    return@runCatching KtfmtFormatSuccess(sourceFile, true)
                }

                if (reformatFiles) {
                    logger.i("Reformatting $sourceFile")
                    sourceFile.writeText(formattedContent, Charset.defaultCharset())
                    return@runCatching KtfmtFormatSuccess(sourceFile, false)
                }

                logger.e("Invalid formatting for: $sourceFile")
                KtfmtDiffer.printDiff(KtfmtDiffer.computeDiff(sourceFile, formattedContent), logger)
                KtfmtFormatSuccess(sourceFile, false)
            }
            .onFailure {
                // When running with --debug, print the full stacktrace for debugging purposes
                logger.e("Failed to format file: $sourceFile (reason = ${it.message})")
                logger.d("Failed to format file: $sourceFile", it)
            }
            .getOrElse { KtfmtFormatFailure(sourceFile) }
    }

    private fun writeResult(result: KtfmtFormatResult) {
        val resultDirectory = parameters.resultDirectory.asFile.get()
        val resultFile = resultDirectory.resolve("${UUID.randomUUID()}.txt")
        resultFile.writeText(result.serialize())
    }

    private fun shouldSkipFile(sourceFile: File, includedFiles: Set<File>): Boolean {
        if (includedFiles.isEmpty()) return false

        return sourceFile.canonicalFile !in includedFiles
    }

    private fun FormattingOptionsBean.toFormattingOptions(): FormattingOptions =
        FormattingOptions(
            maxWidth = maxWidth,
            blockIndent = blockIndent,
            continuationIndent = continuationIndent,
            removeUnusedImports = removeUnusedImports,
            manageTrailingCommas = manageTrailingCommas,
            debuggingPrintOpsAfterFormatting = debuggingPrintOpsAfterFormatting,
        )
}
