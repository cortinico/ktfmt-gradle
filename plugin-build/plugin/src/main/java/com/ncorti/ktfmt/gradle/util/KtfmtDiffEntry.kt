package com.ncorti.ktfmt.gradle.util

import java.io.File

internal data class KtfmtDiffEntry(
    val input: File,
    val lineNumber: Int,
    val message: String
)
