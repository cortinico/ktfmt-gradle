package com.ncorti.ktfmt.gradle

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.testutil.createTempFile
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class PluginVersionCompatibilityTest {

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        File(tempDir, "src/main/java").mkdirs()
        File("src/test/resources/jvmProject-version-compatibility").copyRecursively(tempDir)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1.7.20", "1.9.10", "1.9.20", "2.0.0"])
    fun `plugin can be applied to projects with different kotlin versions`(kotlinVersion: String) {
        replaceKotlinVersion(kotlinVersion)

        tempDir.createTempFile(content = "val answer = 42\n")

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckMain", "--info")
                .build()

        assertThat(result.task(":ktfmtCheckMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun replaceKotlinVersion(version: String) {
        val file = tempDir.resolve("build.gradle.kts")
        val updatedKotlinVersion = file.readText().replace("KOTLIN_VERSION_PLACEHOLDER", version)

        file.writeText(updatedKotlinVersion)
    }
}
