package com.ncorti.ktfmt.gradle.util

import com.github.difflib.DiffUtils
import com.github.difflib.patch.ChangeDelta
import com.github.difflib.patch.DeleteDelta
import com.github.difflib.patch.InsertDelta
import java.io.File
import org.gradle.api.logging.Logger

internal object KtfmtDiffer {

    fun computeDiff(originalFile: File, reformattedCode: String): List<KtfmtDiffEntry> {
        val originalContent = originalFile.readText()

        return DiffUtils.diff(originalContent, reformattedCode, null).deltas.map {
            val line = it.source.position + 1
            val message: String =
                when (it) {
                    is ChangeDelta -> "Line changed: ${it.source.lines.first()}"
                    is DeleteDelta -> "Line deleted"
                    is InsertDelta -> "Line added"
                    else -> ""
                }
            KtfmtDiffEntry(originalFile, line, message)
        }
    }

    fun printDiff(entries: List<KtfmtDiffEntry>, logger: Logger) {
        entries.forEach { entry ->
            logger.i("${entry.input}:${entry.lineNumber} - ${entry.message}")
        }
    }
}
