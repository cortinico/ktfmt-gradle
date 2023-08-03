package com.ncorti.ktfmt.gradle.tasks

import com.facebook.ktfmt.format.ParseError
import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatter
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtResult
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class KtfmtFormatterTest {
    @TempDir lateinit var tempDir: File

    @Test
    fun `formatter returns success for valid file`() {
        val formatCtx = createFormatContext(content = "val hello = \"world\"\n")

        val result = KtfmtFormatter.format(formatCtx) as KtfmtResult.KtfmtSuccess

        assertThat(result.isCorrectlyFormatted).isTrue()
        assertThat(result.input).isEqualTo(formatCtx.sourceFile)
        assertThat(result.formattedCode).isEqualTo("val hello = \"world\"\n")
    }

    @Test
    fun `formatter returns success for reformatted file`() {
        val formatCtx = createFormatContext(content = "val hello=\"world\"")

        val result = KtfmtFormatter.format(formatCtx) as KtfmtResult.KtfmtSuccess

        assertThat(result.isCorrectlyFormatted).isFalse()
        assertThat(result.input).isEqualTo(formatCtx.sourceFile)
        assertThat(result.formattedCode).isEqualTo("val hello = \"world\"\n")
    }

    @Test
    fun `formatter returns failure for parsing failure`() {
        val formatCtx = createFormatContext(content = "val hello=`")
        val result = KtfmtFormatter.format(formatCtx) as KtfmtResult.KtfmtFailure

        assertThat(result.input).isEqualTo(formatCtx.sourceFile)
        assertThat(result.message).isEqualTo("Failed to parse file")
        assertThat(result.reason).isInstanceOf(ParseError::class.java)
    }

    @Test
    fun `formatter skips file outside include-only`() {
        val formatCtx = createFormatContext(content = "val hello=\"world\"", fileName = "file1.kt")
        val input2 = createTempFile(content = "val hello=`", fileName = "file2.kt")
        val newCtx =
            formatCtx.copy(
                sourceFile = input2,
                includedFiles = setOf(formatCtx.sourceFile.relativeTo(tempDir))
            )

        val result = KtfmtFormatter.format(newCtx) as KtfmtResult.KtfmtSkipped

        assertThat(result.input.name).isEqualTo("file2.kt")
        assertThat(result.reason).isEqualTo("Not included inside --include-only")
    }

    @Test
    fun `formatter does not skip file inside include-only`() {
        val formatCtx = createFormatContext(content = "val hello=\"world\"", fileName = "file1.kt")
        val newCtx = formatCtx.copy(includedFiles = setOf(formatCtx.sourceFile.canonicalFile))

        val result = KtfmtFormatter.format(newCtx) as KtfmtResult.KtfmtSuccess

        assertThat(result.isCorrectlyFormatted).isFalse()
        assertThat(result.input.name).isEqualTo("file1.kt")
        assertThat(result.formattedCode).isEqualTo("val hello = \"world\"\n")
    }

    private fun createTempFile(
        content: String,
        fileName: String = "TestFile.kt",
        root: File = tempDir
    ): File =
        File(root, fileName).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(content)
        }

    private fun createFormatContext(
        content: String,
        fileName: String = "TestFile.kt"
    ): KtfmtFormatter.FormatContext {
        val input = createTempFile(content = content, fileName = fileName)
        return KtfmtFormatter.FormatContext(sourceFile = input, sourceRoot = tempDir)
    }
}
