package com.ncorti.ktfmt.gradle

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.file.Path
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class PluginVersionCompatibilityTest {

    @TempDir lateinit var tempDir: File

    private val pluginBuildDirectory = Path.of("./", "../")

    @BeforeEach
    fun setUp() {
        File(tempDir, "src/main/java").mkdirs()
        File("src/test/resources/jvmProject-version-compatibility").copyRecursively(tempDir)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1.7.20", "1.9.10", "1.9.20"])
    fun `plugin can be applied to projects with different kotlin versions`(kotlinVersion: String) {
        // Prevent sharing of classpath
        publishPluginToMavenLocal()

        replaceKotlinVersion(kotlinVersion)

        createTempFile(content = "val answer = 42\n")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withArguments("ktfmtCheckMain", "--info")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun publishPluginToMavenLocal() {
        GradleRunner.create()
            .withProjectDir(pluginBuildDirectory.toFile())
            .withArguments(
                "plugin:publishToMavenLocal",
                "-PVERSION=99.99.99-compatibility-check",
                "-Pskip-signing=true"
            )
            .build()
    }

    private fun replaceKotlinVersion(version: String) {
        val file = tempDir.resolve("build.gradle.kts")
        val updatedKotlinVersion = file.readText().replace("KOTLIN_VERSION_PLACEHOLDER", version)

        file.writeText(updatedKotlinVersion)
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
