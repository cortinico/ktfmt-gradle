package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class KtfmtFormatTaskIntegrationTest {

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        File(tempDir, "src/main/java").mkdirs()
        File("src/test/resources/jvmProject").copyRecursively(tempDir)
    }

    @Test
    fun `format task fails if there is invalid code`() {
        createTempFile(content = "val answer = `")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .buildAndFail()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("e: Failed to parse file")
    }

    @Test
    fun `format formats correctly`() {
        val tempFile = createTempFile(content = "val answer=42")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(tempFile.readText()).isEqualTo("val answer = 42\n")
    }

    @Test
    fun `format task succeed if code is formatted`() {
        createTempFile(content = "val answer = 42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `format task is up to date after subsequent execution`() {
        createTempFile(content = "val answer = 42\n")
        var result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)

        result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(UP_TO_DATE)
    }

    @Test
    fun `format task is up to date after subsequent execution when formatting`() {
        createTempFile(content = "val answer=42")
        var result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()
        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)

        // This run will be triggered as we modified the input (reformatting the file)
        result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()
        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)

        result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()
        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(UP_TO_DATE)
    }

    @Test
    fun `format task is up to executed again after edit`() {
        val tempFile = createTempFile(content = "val answer = 42\n")
        var result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)

        result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(UP_TO_DATE)

        // Let's change file content
        tempFile.writeText("val answer=42\n")

        result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(tempFile.readText()).isEqualTo("val answer = 42\n")
    }

    @Test
    fun `format task prints formatted files with --info`() {
        createTempFile(content = "val answer=42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain", "--info")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.output).contains("[ktfmt] Reformatting...")
        assertThat(result.output).contains("[ktfmt] Successfully reformatted 1 files with Ktfmt")
    }

    @Test
    fun `format task reformats all the file even with a failure`() {
        val file1 = createTempFile(content = "val answer = `", fileName = "File1.kt")
        val file2 = createTempFile(content = "val answer=42", fileName = "File2.kt")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain", "--info")
                .buildAndFail()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(FAILED)

        // File 1 contains a parsing error and is untouched.
        assertThat(file1.readText()).isEqualTo("val answer = `")
        assertThat(file2.readText()).isEqualTo("val answer = 42\n")
        assertThat(result.output).contains("Failed to analyse:")
        assertThat(result.output).contains("Reformatting...:")
    }

    @Test
    fun `format task runs before compilation`() {
        createTempFile(content = "val answer = 42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("compileKotlin", "ktfmtFormatMain", "--dry-run")
                .build()

        assertThat(result.output).contains(":ktfmtFormatMain SKIPPED")
        assertThat(result.output).contains(":compileKotlin SKIPPED")
        assertThat(result.output.indexOf(":ktfmtCheckMain SKIPPED"))
            .isLessThan(result.output.indexOf(":compileKotlin SKIPPED"))
    }

    @Test
    fun `format task skips a file if with --include-only`() {
        createTempFile(content = "val answer = `\n", fileName = "File1.kt")
        val file2 = createTempFile(content = "val answer=42\n", fileName = "File2.kt")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments(
                    "ktfmtFormatMain",
                    "--info",
                    "--include-only=${file2.relativeTo(tempDir)}"
                )
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.output).contains("[ktfmt] Skipping for:")
        assertThat(result.output).contains("Not included inside --include-only")
        assertThat(result.output).contains("[ktfmt] Reformatting...")
        assertThat(result.output).contains("[ktfmt] Successfully reformatted 1 files with Ktfmt")
    }

    private fun createTempFile(
        @Language("kotlin") content: String,
        fileName: String = "TestFile.kt",
        path: String = "src/main/java"
    ) =
        File(File(tempDir, path), fileName).apply {
            createNewFile()
            writeText(content)
        }
}
