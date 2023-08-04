package com.ncorti.ktfmt.gradle.tasks

import java.io.File

internal object IncludedFilesParser {
    fun parse(value: String, rootDir: File): Set<File> {
        if (value.isBlank()) return emptySet()
        return value
            .split(',', ':')
            .map {
                val relativePath = it.removePrefix("/").removePrefix("\\")
                rootDir.resolve(relativePath.replace("\\", "/")).canonicalFile
            }
            .toSet()
    }
}
