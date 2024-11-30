package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.testutil.appendToBuildGradle
import com.ncorti.ktfmt.gradle.testutil.createTempFile
import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class KtfmtCheckTaskIntegrationTest {

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        File("src/test/resources/jvmProject").copyRecursively(tempDir)
    }

    @Test
    fun `check task fails if there is invalid code`() {
        tempDir.createTempFile(content = "val answer = `")
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
        tempDir.createTempFile(content = "val answer=42")
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
        tempDir.createTempFile(
            """
            val res = when {
                ````
            }
            """
                .trimIndent()
        )

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain", "--debug")
                .buildAndFail()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("[ktfmt] Failed to analyse:")
        assertThat(result.output).contains("e: Failed to parse file")
    }

    @Test
    fun `check task succeed if code is formatted`() {
        tempDir.createTempFile(content = "val answer = 42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `check task runs before compilation`() {
        tempDir.createTempFile(content = "val answer = 42\n")
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
        tempDir.createTempFile(content = "val answer = 42\n")
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
        tempDir.createTempFile(content = "val answer = 42\n")
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
        tempDir.createTempFile(content = "val answer = `\n", fileName = "File1.kt")
        tempDir.createTempFile(content = "val answer = 42\n", fileName = "File2.kt")

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
        tempDir.createTempFile(content = "val answer = `\n", fileName = "File1.kt")
        val file2 = tempDir.createTempFile(content = "val answer = 42\n", fileName = "File2.kt")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments(
                    "ktfmtCheckMain",
                    "--debug",
                    "--include-only=${file2.relativeTo(tempDir)}",
                )
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.output).contains("Skipping for:")
        assertThat(result.output).contains("Not included inside --include-only")
        assertThat(result.output).contains("Valid formatting for:")
    }

    @ParameterizedTest
    @ValueSource(ints = [10, 15, 30, 50, 100, 1000])
    fun `check task can check the formatting of multiple files`(n: Int) {
        repeat(n) { index ->
            tempDir.createTempFile(
                content = "val answer${index} = 42\n",
                fileName = "TestFile$index.kt",
            )
        }
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain", "--info")
                .forwardOutput()
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `check task is cacheable`() {
        tempDir.createTempFile(content = "val answer = 42\n")

        var result: BuildResult? = null
        repeat(2) {
            result =
                GradleRunner.create()
                    .withProjectDir(tempDir)
                    .withPluginClasspath()
                    .withArguments("clean", "ktfmtCheckMain", "--build-cache")
                    .build()
        }

        assertThat(result!!.task(":ktfmtCheckMain")?.outcome).isEqualTo(FROM_CACHE)
    }

    @Test
    fun `check task should be up-to-date when invoked twice with multiple different sized sourceSets`() {
        tempDir.createTempFile(
            content = "val answer = 42\n",
            fileName = "SrcFile.kt",
            path = "src/main/java",
        )
        tempDir.createTempFile(
            content = "val answer = 42\n",
            fileName = "TestFile.kt",
            path = "src/test/java",
        )
        tempDir.createTempFile(
            content = "val answer = 42\n",
            fileName = "TestFile2.kt",
            path = "src/test/java",
        )

        val firstRun: BuildResult =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheck", "--info")
                .build()

        assertThat(firstRun.task(":ktfmtCheckMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(firstRun.task(":ktfmtCheckTest")?.outcome).isEqualTo(SUCCESS)

        val secondRun: BuildResult =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheck", "--info")
                .build()

        assertThat(secondRun.task(":ktfmtCheckMain")?.outcome).isEqualTo(UP_TO_DATE)
        assertThat(secondRun.task(":ktfmtCheckTest")?.outcome).isEqualTo(UP_TO_DATE)
    }

    @Test
    fun `check task is configuration cache compatible`() {
        tempDir.createTempFile(content = "val answer = 42\n")

        var result: BuildResult? = null
        repeat(2) {
            result =
                GradleRunner.create()
                    .withProjectDir(tempDir)
                    .withPluginClasspath()
                    .withArguments("ktfmtCheckMain", "--configuration-cache")
                    .build()
        }

        assertThat(result!!.output).contains("Reusing configuration cache.")
    }

    @Test
    fun `custom formatCheck task should be compatible with configuration cache`() {
        tempDir.createTempFile(content = "val answer = 42\n")

        tempDir.appendToBuildGradle(
            """
            |tasks.register<com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask>("customFormatCheck") {
            |    source = fileTree("src/main/java")
            |}
            """
                .trimMargin()
        )

        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("customFormatCheck", "--configuration-cache")
            .build()
    }

    @Test
    fun `check task should detect the source and test files in a flattened project structure`() {
        tempDir.appendToBuildGradle(
            """
            |kotlin {
            |    sourceSets.main { kotlin.setSrcDirs(listOf("src")) }
            |    sourceSets.test { kotlin.setSrcDirs(listOf("test")) }
            |}
        """
                .trimMargin()
        )

        tempDir.createTempFile("val answer = 42\n", path = "src/someFolder")
        tempDir.createTempFile("val answer = 42\n", path = "test/someOtherFolder")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheck")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isNotEqualTo(NO_SOURCE)
        assertThat(result.task(":ktfmtCheckTest")?.outcome).isNotEqualTo(NO_SOURCE)
    }

    @Test
    fun `check task should by default ignore sourceSets in the build folder`() {
        tempDir.appendToBuildGradle(
            """
            |kotlin { sourceSets.main { kotlin.srcDirs("build/main") } }
        """
                .trimMargin()
        )

        tempDir.createTempFile(content = "val answer=42\n", path = "build/main")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheck")
                .forwardOutput()
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(NO_SOURCE)
    }

    @Test
    fun `check task should not ignore sourceSets in build folder when a custom exclusion pattern is specified`() {
        tempDir.appendToBuildGradle(
            """
            |kotlin { sourceSets.main { kotlin.srcDirs("build/generated") } }
            |
            |ktfmt { srcSetPathExclusionPattern.set(Regex("customRules.*")) }
        """
                .trimMargin()
        )

        tempDir.createTempFile(content = "val answer=42\n", path = "build/generated/main")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheck")
                .forwardOutput()
                .buildAndFail()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).containsMatch("Invalid formatting for: .*TestFile.kt")
    }

    @Test
    fun `check task should ignore the main sourceSets when specified as exclusion pattern`() {
        tempDir.appendToBuildGradle(
            """
            |ktfmt { srcSetPathExclusionPattern.set(Regex(".*[\\\\/]main[\\\\/].*")) }
        """
                .trimMargin()
        )

        tempDir.createTempFile(content = "val answer=42\n")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheck")
                .forwardOutput()
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(NO_SOURCE)
    }

    @Test
    fun `check scripts task should validate top-level script file`() {
        tempDir.createTempFile(content = "val answer=42\n", fileName = "TestFile.kts", path = "")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckScripts")
                .buildAndFail()

        assertThat(result.task(":ktfmtCheckScripts")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).containsMatch("Invalid formatting for: .*TestFile.kts")
    }

    @Test
    fun `check scripts task should ignore non top-level script files`() {
        tempDir.createTempFile(
            content = "val answer=42\n",
            fileName = "TestFile.kts",
            path = "src/main/java",
        )

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckScripts")
                .forwardOutput()
                .build()

        assertThat(result.task(":ktfmtCheckScripts")?.outcome).isEqualTo(SUCCESS)
    }
}
