package com.ncorti.ktfmt.gradle.tasks

import java.io.File
import org.intellij.lang.annotations.Language

internal sealed class KtfmtResult(open val input: File)

internal data class KtfmtSuccess(
    override val input: File,
    val isCorrectlyFormatted: Boolean,
    @Language("kotlin") val formattedCode: String
) : KtfmtResult(input)

internal data class KtfmtFailure(
    override val input: File,
    val message: String,
    val reason: Throwable
) : KtfmtResult(input)

internal data class KtfmtSkipped(override val input: File, val reason: String) : KtfmtResult(input)
