package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class KtfmtFormatTaskIntegrationTest {

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
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
    fun `format task uses configuration cache correctly`() {
        createTempFile(content = "val answer = 42\n")
        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("--configuration-cache", "--rerun-tasks", "ktfmtFormatMain")
            .build()

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("--configuration-cache", "--rerun-tasks", "ktfmtFormatMain")
                .build()

        assertThat(result.output).contains("Reusing configuration cache.")
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
                    "ktfmtFormatMain", "--info", "--include-only=${file2.relativeTo(tempDir)}")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.output).contains("[ktfmt] Skipping for:")
        assertThat(result.output).contains("Not included inside --include-only")
        assertThat(result.output).contains("[ktfmt] Reformatting...")
        assertThat(result.output).contains("[ktfmt] Successfully reformatted 1 files with Ktfmt")
    }

    @ParameterizedTest
    @ValueSource(ints = [10, 15, 30, 50, 100, 1000])
    fun `format task can format multiple files`(n: Int) {
        repeat(n) { index ->
            createTempFile(content = "val answer${index}=42\n", fileName = "TestFile$index.kt")
        }
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormatMain", "--info")
                .forwardOutput()
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `custom format task should be compatible with configuration cache`() {
        createTempFile(content = "val answer = 42\n")

        tempDir
            .resolve("build.gradle.kts")
            .appendText(
                """
                |
                |tasks.register<com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask>("customFormatTask") {
                |   source = fileTree("src/main/java")
                |}
                """
                    .trimMargin())

        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("customFormatTask", "--configuration-cache")
            .build()
    }

    @Test
    fun `should format the files in kotlinLang style with a 4 space indentation`() {
        val file =
            createTempFile(
                content =
                    """
                    |fun someFun(){
                    |println("Hello, World!")
                    |println("HelloWorld2")
                    |}
                    """
                        .trimMargin())

        appendToBuildGradle("ktfmt { kotlinLangStyle() }")

        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("ktfmtFormatMain")
            .forwardOutput()
            .build()

        val actual = file.readLines()
        assertThat(actual)
            .containsExactly(
                "fun someFun() {",
                "    println(\"Hello, World!\")",
                "    println(\"HelloWorld2\")",
                "}")
    }

    @Test
    fun `should format the files in googleStyle style with a 2 space indentation`() {
        val file =
            createTempFile(
                content =
                    """
                    |fun someFun(){
                    |println("Hello, World!")
                    |println("HelloWorld2")
                    |}
                    """
                        .trimMargin())

        appendToBuildGradle("ktfmt { googleStyle() }")

        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("ktfmtFormatMain")
            .forwardOutput()
            .build()

        val actual = file.readLines()

        assertThat(actual)
            .containsExactly(
                "fun someFun() {",
                "  println(\"Hello, World!\")",
                "  println(\"HelloWorld2\")",
                "}")
    }

    @Test
    fun `format task should detect the source and test files in a flattened project structure and format them`() {
        appendToBuildGradle(
            """
            |kotlin {
            |    sourceSets.main {
            |       kotlin.setSrcDirs(listOf("src"))
            |    }
            |    sourceSets.test {
            |       kotlin.setSrcDirs(listOf("test"))
            |    }
            |}
        """
                .trimMargin())

        val sourceFile = createTempFile("val answer =  42\n", path = "src/someFolder")
        val testFile = createTempFile("val answer =  42\n", path = "test/someOtherFolder")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormat")
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isNotEqualTo(NO_SOURCE)
        assertThat(result.task(":ktfmtFormatTest")?.outcome).isNotEqualTo(NO_SOURCE)

        assertThat(sourceFile.readText()).contains("val answer = 42\n")
        assertThat(testFile.readText()).contains("val answer = 42\n")
    }

    @Test
    fun `format task should by default not format sourceSets in the build folder`() {
        appendToBuildGradle(
            """
            |kotlin {
            |    sourceSets.main {
            |       kotlin.srcDirs("build/main")
            |    }
            |}
        """
                .trimMargin())

        val file = createTempFile(content = "val answer=42\n", path = "build/main")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormat")
                .forwardOutput()
                .build()

        val actual = file.readText()
        assertThat(actual).isEqualTo("val answer=42\n")
        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(NO_SOURCE)
    }

    @Test
    fun `format task should not ignore sourceSets in build folder when a custom exclusion pattern is specified`() {
        appendToBuildGradle(
            """
            |kotlin {
            |    sourceSets.main {
            |       kotlin.srcDirs("build/generated")
            |    }
            |}
            |
            |ktfmt{
            |    srcSetPathExclusionPattern.set(Regex("customRules.*"))
            |}
        """
                .trimMargin())

        val file = createTempFile(content = "val answer=42\n", path = "build/generated/main")

        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("ktfmtFormat", "--info")
            .forwardOutput()
            .build()

        val actual = file.readText()
        assertThat(actual).isEqualTo("val answer = 42\n")
    }

    @Test
    fun `format task should ignore the main sourceSets when specified as exclusion pattern`() {
        appendToBuildGradle(
            """
            |ktfmt{
            |    srcSetPathExclusionPattern.set(Regex(".*/main/.*"))
            |}
        """
                .trimMargin())

        createTempFile(content = "val answer=42\n")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtFormat")
                .forwardOutput()
                .build()

        assertThat(result.task(":ktfmtFormatMain")?.outcome).isEqualTo(NO_SOURCE)
    }

    private fun createTempFile(
        @Language("kotlin") content: String,
        fileName: String = "TestFile.kt",
        path: String = "src/main/java"
    ): File =
        tempDir.resolve(path).resolve(fileName).apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(content)
        }

    private fun appendToBuildGradle(content: String) {
        tempDir.resolve("build.gradle.kts").apply {
            appendText(System.lineSeparator())
            appendText(content)
        }
    }
}
