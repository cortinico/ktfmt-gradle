package com.ncorti.ktfmt.gradle.tasks.worker

import com.facebook.ktfmt.format.Formatter
import com.facebook.ktfmt.format.FormattingOptions
import com.facebook.ktfmt.format.ParseError
import com.google.googlejavaformat.FormattingError
import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import com.ncorti.ktfmt.gradle.util.d
import java.io.File
import java.io.IOException
import org.gradle.api.logging.Logging

// format the specific file and return the result
// this implementation is decoupled from Gradle APIs for ease of testing
internal object KtfmtFormatter {
    private val logger = Logging.getLogger(KtfmtFormatter::class.java)

    data class FormatContext(
        val sourceFile: File,
        val includedFiles: Set<File> = emptySet(),
        val formattingOptions: FormattingOptionsBean = FormattingOptionsBean(),
        val sourceRoot: File
    )

    fun format(ctx: FormatContext): KtfmtResult {
        logger.d("Formatting: $ctx")
        if (ctx.includedFiles.isNotEmpty()) {
            if (ctx.sourceFile.canonicalFile !in ctx.includedFiles) {
                return KtfmtResult.KtfmtSkipped(
                    ctx.sourceFile, "Not included inside --include-only")
            }
        }
        @Suppress("TooGenericExceptionCaught")
        return try {
            val originCode = ctx.sourceFile.readText()
            val formattedCode =
                Formatter.format(ctx.formattingOptions.toFormattingOptions(), originCode)
            val isCorrectlyFormatted = originCode == formattedCode
            KtfmtResult.KtfmtSuccess(ctx.sourceFile, isCorrectlyFormatted, formattedCode)
        } catch (cause: Throwable) {
            val message =
                when (cause) {
                    is IOException -> "Unable to read file"
                    is ParseError -> "Failed to parse file"
                    is FormattingError ->
                        cause.diagnostics().joinToString("\n") { "${ctx.sourceFile}:$it" }
                    else -> "Generic error during file processing"
                }
            KtfmtResult.KtfmtFailure(ctx.sourceFile, message, cause)
        }
    }
}

internal fun FormattingOptionsBean.toFormattingOptions(): FormattingOptions =
    FormattingOptions(
        maxWidth = maxWidth,
        blockIndent = blockIndent,
        continuationIndent = continuationIndent,
        removeUnusedImports = removeUnusedImports,
        manageTrailingCommas = manageTrailingCommas,
        debuggingPrintOpsAfterFormatting = debuggingPrintOpsAfterFormatting,
    )
