package com.ncorti.ktfmt.gradle.tasks

import com.facebook.ktfmt.ParseError
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class KtfmtBaseTaskTest {

    private lateinit var project: Project

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")
    }

    @Test
    fun `inputFile is the same as source files`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        assertThat(underTest.inputs.files.toList()).isEqualTo(underTest.source.files.toList())
    }

    @Test
    fun `outputFile is the same as source files`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        assertThat(underTest.outputs.files.toList()).isEqualTo(underTest.source.files.toList())
    }

    @Test
    fun `scope is not canceled before running`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        assertThat(underTest.isScopeActive()).isTrue()
    }

    @Test
    fun `scope is canceled after running`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask
        underTest.taskAction()

        assertThat(underTest.isScopeActive()).isFalse()
    }

    @Test
    fun `processFile returns success for valid file`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input = createTempFile(content = "val hello = \"world\"\n")
        val result = underTest.processFile(input) as KtfmtSuccess

        assertThat(result.isCorrectlyFormatted).isTrue()
        assertThat(result.input).isEqualTo(input)
        assertThat(result.formattedCode).isEqualTo("val hello = \"world\"\n")
    }

    @Test
    fun `processFile returns success for reformatted file`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input = createTempFile(content = "val hello=\"world\"")
        val result = underTest.processFile(input) as KtfmtSuccess

        assertThat(result.isCorrectlyFormatted).isFalse()
        assertThat(result.input).isEqualTo(input)
        assertThat(result.formattedCode).isEqualTo("val hello = \"world\"\n")
    }

    @Test
    fun `processFile returns failure for parsing failure`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input = createTempFile(content = "val hello=`")
        val result = underTest.processFile(input) as KtfmtFailure

        assertThat(result.input).isEqualTo(input)
        assertThat(result.message).isEqualTo("Failed to parse file")
        assertThat(result.reason).isInstanceOf(ParseError::class.java)
    }

    @Test
    fun `processFile skips file outside include-only`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input1 = createTempFile(content = "val hello=`", fileName = "file1.kt")
        val input2 = createTempFile(content = "val hello=\"world\"", fileName = "file2.kt")

        underTest.includeOnly.set(input1.relativeTo(tempDir).toString())

        val result = underTest.processFile(project.file("file2.kt")) as KtfmtSkipped

        assertThat(result.input.name).isEqualTo("file2.kt")
        assertThat(result.reason).isEqualTo("Not included inside --include-only")
    }

    @Test
    fun `processFile does not skip file inside include-only`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input1 = createTempFile(content = "val hello=`", fileName = "file1.kt")
        val input2 = createTempFile(content = "val hello=\"world\"", fileName = "file2.kt")

        underTest.includeOnly.set(input2.relativeTo(tempDir).toString())

        val result = underTest.processFile(project.file("file2.kt")) as KtfmtSuccess

        assertThat(result.isCorrectlyFormatted).isFalse()
        assertThat(result.input.name).isEqualTo("file2.kt")
        assertThat(result.formattedCode).isEqualTo("val hello = \"world\"\n")
    }

    @Test
    fun `processFile skips file inside comma separated include-only`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input1 = createTempFile(content = "val hello=`", fileName = "file1.kt")
        val input2 = createTempFile(content = "val hello=\"world\"", fileName = "file2.kt")

        underTest.includeOnly.set("${input1.relativeTo(tempDir)},${input2.relativeTo(tempDir)}")

        underTest.processFile(project.file("file1.kt")) as KtfmtFailure
        underTest.processFile(project.file("file2.kt")) as KtfmtSuccess
    }

    @Test
    fun `processFile skips file inside colon separated include-only`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input1 = createTempFile(content = "val hello=`", fileName = "file1.kt")
        val input2 = createTempFile(content = "val hello=\"world\"", fileName = "file2.kt")

        underTest.includeOnly.set("${input1.relativeTo(tempDir)}:${input2.relativeTo(tempDir)}")

        underTest.processFile(project.file("file1.kt")) as KtfmtFailure
        underTest.processFile(project.file("file2.kt")) as KtfmtSuccess
    }

    @Test
    fun `processFileCollection with multiple files works correctly`() = runBlocking {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input1 = createTempFile(fileName = "file1.kt", content = "val hello = 1\n")
        val input2 = createTempFile(fileName = "file2.kt", content = "val hello = 2")

        val result = underTest.processFileCollection(project.files(input1, input2))

        assertThat(result).hasSize(2)
        assertThat(result.filterIsInstance<KtfmtSuccess>()).hasSize(2)
        assertThat((result[0] as KtfmtSuccess).isCorrectlyFormatted).isTrue()
        assertThat((result[1] as KtfmtSuccess).isCorrectlyFormatted).isFalse()
    }

    @Test
    fun `processFileCollection with a failure keeps on formatting`() = runBlocking {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        val input1 = createTempFile(fileName = "file1.kt", content = "val hello = 1\n")
        val input2 = createTempFile(fileName = "file2.kt", content = "val hello=`")

        val result = underTest.processFileCollection(project.files(input1, input2))

        assertThat(result).hasSize(2)
        assertThat(result.filterIsInstance<KtfmtSuccess>()).hasSize(1)
        assertThat((result[0] as KtfmtSuccess).isCorrectlyFormatted).isTrue()
        assertThat((result[1] as KtfmtFailure).reason).isInstanceOf(ParseError::class.java)
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
