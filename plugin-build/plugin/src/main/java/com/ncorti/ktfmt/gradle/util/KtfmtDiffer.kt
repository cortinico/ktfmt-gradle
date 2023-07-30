package com.ncorti.ktfmt.gradle.util

import com.github.difflib.DiffUtils
import com.github.difflib.patch.ChangeDelta
import com.github.difflib.patch.DeleteDelta
import com.github.difflib.patch.InsertDelta
import com.ncorti.ktfmt.gradle.tasks.KtfmtResult
import org.gradle.api.logging.Logger

internal object KtfmtDiffer {

    fun computeDiff(formatterResult: KtfmtResult.KtfmtSuccess): List<KtfmtDiffEntry> {
        val input = formatterResult.input.readText()
        val output = formatterResult.formattedCode
        return DiffUtils.diff(input, output, null).deltas.map {
            val line = it.source.position + 1
            val message: String =
                when (it) {
                    is ChangeDelta -> "Line changed: ${it.source.lines.first()}"
                    is DeleteDelta -> "Line deleted"
                    is InsertDelta -> "Line added"
                    else -> ""
                }
            KtfmtDiffEntry(formatterResult.input, line, message)
        }
    }

    fun printDiff(entries: List<KtfmtDiffEntry>, logger: Logger) {
        entries.forEach { entry ->
            logger.i("${entry.input}:${entry.lineNumber} - ${entry.message}")
        }
    }
}
