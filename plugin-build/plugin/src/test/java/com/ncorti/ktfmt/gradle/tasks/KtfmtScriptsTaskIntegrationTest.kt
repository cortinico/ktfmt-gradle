package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.testutil.createTempFile
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFileAttributeView
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class KtfmtScriptsTaskIntegrationTest {

    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        File("src/test/resources/jvmProject").copyRecursively(tempDir)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ktfmtCheckScripts", "ktfmtFormatScripts"])
    fun `scripts task skips unreadable subdirectory contents`(taskName: String) {
        tempDir.createTempFile(
            content = "val answer = 42\n",
            fileName = "root-script.kts",
            path = "",
        )
        val unreadableDirectory = tempDir.resolve("build/unreadable").apply { mkdirs() }
        val unreadablePath = unreadableDirectory.toPath()
        val supportsPosixPermissions =
            Files.getFileStore(unreadablePath)
                .supportsFileAttributeView(PosixFileAttributeView::class.java)
        assumeTrue(supportsPosixPermissions, "POSIX file permissions are not supported")

        val originalPermissions = Files.getPosixFilePermissions(unreadablePath)
        try {
            Files.setPosixFilePermissions(unreadablePath, emptySet())
            assumeFalse(Files.isReadable(unreadablePath), "Process can still read the directory")

            val result =
                GradleRunner.create()
                    .withProjectDir(tempDir)
                    .withPluginClasspath()
                    .withArguments(taskName)
                    .build()

            assertThat(result.task(":$taskName")?.outcome).isEqualTo(SUCCESS)
        } finally {
            Files.setPosixFilePermissions(unreadablePath, originalPermissions)
        }
    }

    @Test
    fun `check scripts task discovers new script after configuration cache reuse`() {
        tempDir.createTempFile(
            content = "val answer = 42\n",
            fileName = "existing-script.kts",
            path = "",
        )
        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments("ktfmtCheckScripts", "--configuration-cache")
            .build()

        tempDir.createTempFile(content = "val answer=42\n", fileName = "new-script.kts", path = "")
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtCheckScripts", "--configuration-cache")
                .buildAndFail()

        assertThat(result.output).contains("Reusing configuration cache.")
        assertThat(result.output).containsMatch("Invalid formatting for: .*new-script.kts")
    }
}
