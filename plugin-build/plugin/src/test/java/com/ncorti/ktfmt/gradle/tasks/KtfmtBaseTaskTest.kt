package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import java.io.File
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
    fun `outputFile is the same as source files`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask

        assertThat(underTest.outputs.files.toList()).isEqualTo(underTest.source.files.toList())
    }

    @Test
    fun `regular files are not excluded`() {
        val underTest = project.tasks.getByName("ktfmtFormatMain") as KtfmtBaseTask
        val file = createTempFile(fileName = "file.kt", content = "")
        underTest.source(file)

        assertThat(underTest.source.files.contains(file)).isTrue()
    }

    private fun createTempFile(
        @Language("kotlin") content: String,
        fileName: String = "TestFile.kt",
        root: File = tempDir,
    ): File =
        File(root, fileName).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(content)
        }
}
