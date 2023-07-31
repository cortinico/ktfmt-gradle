package com.ncorti.ktfmt.gradle.util

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.tasks.KtfmtResult.KtfmtSuccess
import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class KtfmtDifferTest {

    @TempDir lateinit var tempDir: File

    val logger = TestLogger(ProjectBuilder.builder().build())

    @Test
    fun `computeDiff with empty diff returns empty`() {
        val inputFile = createTempFile(content = "")
        val input = KtfmtSuccess(inputFile, isCorrectlyFormatted = true, formattedCode = "")

        val diff = KtfmtDiffer.computeDiff(input)

        assertThat(diff).isEmpty()
    }

    @Test
    fun `computeDiff with deleted line returns valid diff`() {
        val inputFile =
            createTempFile(
                content =
                    """
                val a = "So long,"
                val b = "and thanks for all the fish!"
            """
                        .trimIndent()
            )

        val input =
            KtfmtSuccess(
                inputFile,
                isCorrectlyFormatted = false,
                formattedCode =
                    """
                val a = "So long,"
            """
                        .trimIndent()
            )

        val diff = KtfmtDiffer.computeDiff(input)

        assertThat(diff).hasSize(1)
        assertThat(diff.first().lineNumber).isEqualTo(2)
        assertThat(diff.first().message).isEqualTo("Line deleted")
    }

    @Test
    fun `computeDiff with added line returns valid diff`() {
        val inputFile =
            createTempFile(
                content =
                    """
                val a = "So long,"
            """
                        .trimIndent()
            )

        val input =
            KtfmtSuccess(
                inputFile,
                isCorrectlyFormatted = false,
                formattedCode =
                    """
                val a = "So long,"
                val b = "and thanks for all the fish!"
            """
                        .trimIndent()
            )

        val diff = KtfmtDiffer.computeDiff(input)

        assertThat(diff).hasSize(1)
        assertThat(diff.first().lineNumber).isEqualTo(2)
        assertThat(diff.first().message).isEqualTo("Line added")
    }

    @Test
    fun `computeDiff with changed line returns valid diff`() {
        val inputFile =
            createTempFile(
                content =
                    """
                  val a = "So long,"
                val b = "and thanks for all the fish!"
            """
                        .trimIndent()
            )

        val input =
            KtfmtSuccess(
                inputFile,
                isCorrectlyFormatted = false,
                formattedCode =
                    """
                val a = "So long,"
                val b = "and thanks for all the fish!"
            """
                        .trimIndent()
            )

        val diff = KtfmtDiffer.computeDiff(input)

        assertThat(diff).hasSize(1)
        assertThat(diff.first().lineNumber).isEqualTo(1)
        assertThat(diff.first().message).isEqualTo("Line changed:   val a = \"So long,\"")
    }

    @Test
    fun `computeDiff with multiple changed lines returns multiple entries`() {
        val inputFile =
            createTempFile(
                content =
                    """
                   val a = "So long,"
                val b = "and thanks!"
                 val c = "for all the fish!"
            """
                        .trimIndent()
            )

        val input =
            KtfmtSuccess(
                inputFile,
                isCorrectlyFormatted = false,
                formattedCode =
                    """
                val a = "So long,"
                val b = "and thanks!"
                val c = "for all the fish!"
            """
                        .trimIndent()
            )

        val diff = KtfmtDiffer.computeDiff(input)

        assertThat(diff).hasSize(2)

        assertThat(diff[0].lineNumber).isEqualTo(1)
        assertThat(diff[0].message).isEqualTo("Line changed:    val a = \"So long,\"")
        assertThat(diff[1].lineNumber).isEqualTo(3)
        assertThat(diff[1].message).isEqualTo("Line changed:  val c = \"for all the fish!\"")
    }

    @Test
    fun `printDiff adds valid line numbers`() {
        val inputFile = createTempFile(content = "val a = 42\nval b = 24", fileName = "TestFile.kt")
        val input = KtfmtSuccess(inputFile, false, "val a = 42")

        KtfmtDiffer.printDiff(KtfmtDiffer.computeDiff(input), logger)

        assertThat(logger.infoMessages).hasSize(1)
        assertThat(logger.infoMessages.first()).isEqualTo("[ktfmt] $inputFile:2 - Line deleted")
    }

    private fun createTempFile(
        @Language("kotlin") content: String,
        fileName: String = "TestFile.kt"
    ): File =
        File(tempDir, fileName).apply {
            createNewFile()
            writeText(content)
        }
}
