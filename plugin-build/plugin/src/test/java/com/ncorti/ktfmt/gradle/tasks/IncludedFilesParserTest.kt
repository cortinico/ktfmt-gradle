package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class IncludedFilesParserTest {
    @TempDir lateinit var tempDir: File

    @Test
    fun `include path parser normalizes separator`() {

        val input = tempDir.resolve("abc.txt")
        val path = input.relativeTo(tempDir).toString()
        val slashPath = path.replace("\\", "/")
        val backSlashPath = path.replace("/", "\\")

        val includePaths = IncludedFilesParser.parse("$slashPath:$backSlashPath", tempDir)

        assertThat(includePaths).hasSize(1)
    }

    @Test
    fun `include path parser handles comma and colon separators`() {
        val includePaths = IncludedFilesParser.parse("/a/b:/c/d,/e/f:/g/h", tempDir)

        assertThat(includePaths).hasSize(4)

        val tmp = tempDir.canonicalFile.toString()
        val sep = File.separator

        assertThat(includePaths.map { it.toString() })
            .containsExactly(
                "$tmp${sep}a${sep}b",
                "$tmp${sep}c${sep}d",
                "$tmp${sep}e${sep}f",
                "$tmp${sep}g${sep}h",
            )
    }
}
