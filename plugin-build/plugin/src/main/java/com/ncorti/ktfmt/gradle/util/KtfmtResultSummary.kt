package com.ncorti.ktfmt.gradle.util

import java.io.File

data class KtfmtResultSummary(
    val validFormattedFiles: List<File>,
    val invalidFormattedFiles: List<File>,
    val skippedFiles: List<File>,
    val failedFiles: List<File>,
) {

    fun prettyPrint(): String {
        return """
        |Ktfmt Summary:
        |  - Valid formatted files: ${validFormattedFiles.size}
        |  - Invalid formatted files: ${invalidFormattedFiles.size}
        |  - Skipped files: ${skippedFiles.size}
        |  - Failed files: ${failedFiles.size}
        """
            .trimMargin()
    }
}
