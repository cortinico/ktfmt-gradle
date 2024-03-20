package com.ncorti.ktfmt.gradle.tasks

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.incremental.createDirectory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GitPreCommitHookIntegrationTest {
    @TempDir lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        File(tempDir, "src/main/java").mkdirs()
        File("src/test/resources/jvmProject").copyRecursively(tempDir)
    }

    @Test
    fun `it does not create the pre commit hook if git directory does not exist`() {
        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtGenerateGitPreCommitHook")
                .build()

        assertThat(result.task(":ktfmtGenerateGitPreCommitHook")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output)
            .contains("[ktfmt] Git directory not found, will not create the hook")
        assertThat(File("${tempDir.path}/.git/hooks/pre-commit").exists()).isFalse()
    }

    @Test
    fun `it does create the pre commit hook if git directory exists`() {
        val gitDirectory = File("${tempDir.path}/.git")
        gitDirectory.createDirectory()

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtGenerateGitPreCommitHook")
                .build()

        assertThat(result.task(":ktfmtGenerateGitPreCommitHook")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(File("${tempDir.path}/.git/hooks/pre-commit").exists()).isTrue()
        assertThat(File("${tempDir.path}/.git/hooks/pre-commit").exists()).isTrue()

        // clean up
        gitDirectory.delete()
    }

    @Test
    fun `it does not create the pre commit hook if git directory exists but hook also already exists`() {
        val gitDirectory = File("${tempDir.path}/.git")

        gitDirectory.createDirectory()

        val hookFile = File("${tempDir.path}/.git/hooks/pre-commit")

        hookFile.ensureParentDirsCreated()
        hookFile.createNewFile()

        hookFile.writeText(
            """
            #!/bin/bash
            echo exists;
        """
                .trimIndent()
        )

        val result =
            GradleRunner.create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("ktfmtGenerateGitPreCommitHook")
                .build()

        assertThat(result.task(":ktfmtGenerateGitPreCommitHook")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(File("${tempDir.path}/.git/hooks/pre-commit").exists()).isTrue()
        assertThat(result.output)
            .contains("[ktfmt] Pre commit hook file already exists, aborting hook generation")

        assertThat(hookFile.readText())
            .isEqualTo(
                """
            #!/bin/bash
            echo exists;
        """
                    .trimIndent()
            )

        // clean up
        gitDirectory.delete()
    }
}
