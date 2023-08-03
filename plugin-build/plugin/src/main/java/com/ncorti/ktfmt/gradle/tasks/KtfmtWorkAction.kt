package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.FormattingOptionsBean
import com.ncorti.ktfmt.gradle.tasks.KtfmtResult.*
import com.ncorti.ktfmt.gradle.util.KtfmtDiffer
import com.ncorti.ktfmt.gradle.util.e
import com.ncorti.ktfmt.gradle.util.i
import java.io.File
import java.nio.charset.Charset
import java.util.*
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.intellij.lang.annotations.Language

/**
 * Base work action to run ktfmt in isolated classloader.
 *
 * This class (and its collaborators, such as KtfmtFormatter) is the ONLY class that should use
 * classes from ktfmt (`com.facebook.ktfmt`) to maintain classloader isolation.
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
        val includedFiles: SetProperty<File>
        val projectDir: DirectoryProperty
        val workingDir: DirectoryProperty
    }

    final override fun execute() {
        val input = parameters.sourceFile.asFile.get()
        val result = processFile(input)
        handleResult(result)
    }

    private fun processFile(input: File): KtfmtResult {
        val ctx =
            KtfmtFormatter.FormatContext(
                sourceFile = input,
                sourceRoot = parameters.projectDir.asFile.get(),
                includedFiles = parameters.includedFiles.get(),
                formattingOptions = parameters.formattingOptions.get()
            )
        val result = KtfmtFormatter.format(ctx)
        writeResult(result)
        return result
    }

    private fun writeResult(result: KtfmtResult) {
        val resultFile = parameters.workingDir.asFile.get().resolve(UUID.randomUUID().toString())
        resultFile.writeText(result.toResultString())
    }

    private fun KtfmtResult.toResultString(): String {
        val relativePath = input.relativeTo(parameters.projectDir.asFile.get()).path
        var correctlyFormatted = false
        val status =
            when (this) {
                is KtfmtSuccess -> {
                    correctlyFormatted = this.isCorrectlyFormatted
                    "success"
                }
                is KtfmtFailure -> "failure"
                is KtfmtSkipped -> "skipped"
            }

        return "$status,$correctlyFormatted,$relativePath"
    }

    internal abstract fun handleResult(result: KtfmtResult)
}

internal abstract class KtfmtFormatAction : KtfmtWorkAction() {
    override fun handleResult(result: KtfmtResult) {
        when {
            result is KtfmtSuccess && result.isCorrectlyFormatted ->
                logger.i("Valid formatting for: ${result.input}")
            result is KtfmtSuccess && !result.isCorrectlyFormatted -> {
                logger.i("Reformatting...: ${result.input}")
                result.input.writeText(result.formattedCode, Charset.defaultCharset())
            }
            result is KtfmtSkipped ->
                logger.i("Skipping for: ${result.input} because: ${result.reason}")
            result is KtfmtFailure -> {
                logger.e("Failed to analyse: ${result.input}")
                result.message.split("\n").forEach { line -> logger.e("e: $line") }
            }
        }
    }
}

internal abstract class KtfmtCheckAction : KtfmtWorkAction() {
    override fun handleResult(result: KtfmtResult) {
        when {
            result is KtfmtSuccess && result.isCorrectlyFormatted ->
                logger.i("Valid formatting for: ${result.input}")
            result is KtfmtSuccess && !result.isCorrectlyFormatted -> {
                logger.e("Invalid formatting for: ${result.input}")
                KtfmtDiffer.printDiff(KtfmtDiffer.computeDiff(result), logger)
            }
            result is KtfmtSkipped ->
                logger.i("Skipping for: ${result.input} because: ${result.reason}")
            result is KtfmtFailure -> {
                logger.e("Failed to analyse: ${result.input}")
                result.message.split("\n").forEach { line -> logger.e("e: $line") }
            }
        }
    }
}

// used for marshalling results from WorkAction back to caller
internal sealed class Result(open val relativePath: String) {
    data class Success(override val relativePath: String, val correctlyFormatted: Boolean) :
        Result(relativePath)

    data class Skipped(override val relativePath: String) : Result(relativePath)

    data class Failure(override val relativePath: String) : Result(relativePath)

    companion object {
        fun fromResultString(str: String): Result {
            val pieces = str.split(",")
            check(pieces.size == 3) { "Expected three components; got $str" }
            return when (pieces[0]) {
                "success" -> Success(pieces[2], pieces[1].toBoolean())
                "skipped" -> Skipped(pieces[2])
                "failure" -> Failure(pieces[2])
                else -> error("Unknown status: ${pieces[0]}")
            }
        }
    }
}

// used for internal processing within WorkAction
internal sealed class KtfmtResult(open val input: File) {
    data class KtfmtSuccess(
        override val input: File,
        val isCorrectlyFormatted: Boolean,
        @Language("kotlin") val formattedCode: String
    ) : KtfmtResult(input)

    data class KtfmtFailure(override val input: File, val message: String, val reason: Throwable) :
        KtfmtResult(input)

    data class KtfmtSkipped(override val input: File, val reason: String) : KtfmtResult(input)
}
