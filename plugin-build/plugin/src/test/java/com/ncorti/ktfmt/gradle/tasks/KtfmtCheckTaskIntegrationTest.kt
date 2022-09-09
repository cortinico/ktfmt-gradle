package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class KtfmtCheckTaskIntegrationTest {

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        File(tempDir, "src/main/java").mkdirs()
        File("src/test/resources/jvmProject").copyRecursively(tempDir)
    }

    @Test
    fun `check task fails if there is invalid code`() {
        createTempFile(content = "val answer = `")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain")
                .buildAndFail()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("e: Failed to parse file")
    }

    @Test
    fun `check task fails if there is not formatted code`() {
        createTempFile(content = "val answer=42")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain")
                .buildAndFail()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("[ktfmt] Invalid formatting")
    }

    @Test
    fun `check task fails if ktfmt fails to parse the code`() {
        // The following code will case ktfmt to fail with error: Failed to parse file
        createTempFile(
            """
            val res = when {
                ````
            }
            """.trimIndent(
            )
        )

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain")
                .buildAndFail()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("[ktfmt] Failed to analyse:")
        assertThat(result.output).contains("e: Failed to parse file")
    }

    @Test
    fun `check task succeed if code is formatted`() {
        createTempFile(content = "val answer = 42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `check task is always executed on subsequent execution`() {
        createTempFile(content = "val answer = 42\n")
        var result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)

        result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `check task runs before compilation`() {
        createTempFile(content = "val answer = 42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("compileKotlin", "ktfmtCheckMain", "--dry-run")
                .build()

        assertThat(result.output).contains(":ktfmtCheckMain SKIPPED")
        assertThat(result.output).contains(":compileKotlin SKIPPED")
        assertThat(result.output.indexOf(":ktfmtCheckMain SKIPPED"))
            .isLessThan(result.output.indexOf(":compileKotlin SKIPPED"))
    }

    @Test
    fun `check task prints formatted files with --info`() {
        createTempFile(content = "val answer = 42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain", "--info")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.output).contains("[ktfmt] Successfully checked 1 files with Ktfmt")
        assertThat(result.output).contains("[ktfmt] Valid formatting")
    }

    @Test
    fun `format task uses configuration cache correctly`() {
        createTempFile(content = "val answer = 42\n")
        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("--configuration-cache", "--rerun-tasks", "ktfmtCheckMain")
            .build()

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("--configuration-cache", "--rerun-tasks", "ktfmtCheckMain")
                .build()

        assertThat(result.output).contains("Reusing configuration cache.")
    }

    @Test
    fun `check task validates all the file with a failure`() {
        createTempFile(content = "val answer = `\n", fileName = "File1.kt")
        createTempFile(content = "val answer = 42\n", fileName = "File2.kt")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain", "--info")
                .buildAndFail()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("Failed to parse file")
        assertThat(result.output).contains("Valid formatting for:")
    }

    @Test
    fun `check task skips a file if with --include-only`() {
        createTempFile(content = "val answer = `\n", fileName = "File1.kt")
        val file2 = createTempFile(content = "val answer = 42\n", fileName = "File2.kt")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments(
                    "ktfmtCheckMain",
                    "--info",
                    "--include-only=${file2.relativeTo(tempDir)}"
                )
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.output).contains("Skipping for:")
        assertThat(result.output).contains("Not included inside --include-only")
        assertThat(result.output).contains("Valid formatting for:")
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
