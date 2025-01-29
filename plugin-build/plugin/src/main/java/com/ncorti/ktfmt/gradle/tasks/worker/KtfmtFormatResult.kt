package com.ncorti.ktfmt.gradle.tasks.worker

import java.io.File

internal sealed class KtfmtFormatResult(open val input: File) {

    abstract fun serialize(): String

    data class KtfmtFormatSuccess(override val input: File, val wasCorrectlyFormatted: Boolean) :
        KtfmtFormatResult(input) {

        override fun serialize(): String = "success,$wasCorrectlyFormatted,${input.path}"
    }

    data class KtfmtFormatFailure(override val input: File) : KtfmtFormatResult(input) {

        override fun serialize(): String = "failure,false,${input.path}"
    }

    data class KtfmtFormatSkipped(override val input: File) : KtfmtFormatResult(input) {

        override fun serialize(): String = "skipped,false,${input.path}"
    }

    companion object {
        fun parse(str: String): KtfmtFormatResult {
            val pieces = str.split(",")

            @Suppress("MagicNumber")
            check(pieces.size == 3) { "Expected three components; got $str" }
            val (status, wasCorrectlyFormatted, filePath) = pieces
            return when (status) {
                "success" -> KtfmtFormatSuccess(File(filePath), wasCorrectlyFormatted.toBoolean())
                "skipped" -> KtfmtFormatSkipped(File(filePath))
                "failure" -> KtfmtFormatFailure(File(filePath))
                else -> error("Unknown status: ${pieces[0]}")
            }
        }
    }
}

internal fun Sequence<KtfmtFormatResult>.files(): List<File> = this.map { it.input }.toList()
