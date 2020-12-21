package com.ncorti.ktfmt.gradle

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class KtfmtPluginTest {

    @Test
    fun `plugin is applied correctly to a plain project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        assertThat(project["ktfmtCheck"].dependsOn).isEmpty()
        assertThat(project["ktfmtFormat"].dependsOn).isEmpty()
    }

    @Test
    fun `plugin is applied correctly to a kotlin jvm project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        assertThat(project["ktfmtCheckMain"]).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project["ktfmtFormatMain"]).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project["ktfmtCheckTest"]).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project["ktfmtFormatTest"]).isInstanceOf(KtfmtFormatTask::class.java)

        assertThat(project["ktfmtCheck"].dependencies).containsExactly("ktfmtCheckMain", "ktfmtCheckTest")
        assertThat(project["ktfmtFormat"].dependencies).containsExactly("ktfmtFormatMain", "ktfmtFormatTest")
    }

    @Test
    fun `plugin is applied correctly to a kotlin js project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.js")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        assertThat(project["ktfmtCheckMain"]).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project["ktfmtFormatMain"]).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project["ktfmtCheckTest"]).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project["ktfmtFormatTest"]).isInstanceOf(KtfmtFormatTask::class.java)

        assertThat(project["ktfmtCheck"].dependencies).containsExactly("ktfmtCheckMain", "ktfmtCheckTest")
        assertThat(project["ktfmtFormat"].dependencies).containsExactly("ktfmtFormatMain", "ktfmtFormatTest")
    }

    @Test
    fun `plugin is applied correctly to a kotlin multiplatform project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        assertThat(project.tasks.getByName("ktfmtCheckCommonMain")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatCommonMain")).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckCommonTest")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatCommonTest")).isInstanceOf(KtfmtFormatTask::class.java)

        assertThat(project["ktfmtCheck"].dependencies).containsExactly(
            "ktfmtCheckCommonMain",
            "ktfmtCheckCommonTest"
        )
        assertThat(project["ktfmtFormat"].dependencies).containsExactly(
            "ktfmtFormatCommonMain",
            "ktfmtFormatCommonTest"
        )
    }

    @Test
    fun `plugin is applied correctly to a kotlin android project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.android.application")
        project.pluginManager.apply("org.jetbrains.kotlin.android")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        assertDoesNotThrow {
            project.tasks.getByName("ktfmtCheck")
            project.tasks.getByName("ktfmtFormat")
        }

        // AndroidTest
        assertThat(project.tasks.getByName("ktfmtCheckAndroidTestDebugJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckAndroidTestReleaseJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckAndroidTestJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatAndroidTestDebugJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatAndroidTestReleaseJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatAndroidTestJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)

        // Test
        assertThat(project.tasks.getByName("ktfmtCheckTestDebugJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckTestReleaseJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckTestJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestDebugJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestReleaseJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)

        // Prod
        assertThat(project.tasks.getByName("ktfmtCheckDebugJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckReleaseJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckMainJavaSource")).isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatDebugJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatReleaseJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatMainJavaSource")).isInstanceOf(KtfmtFormatTask::class.java)

        assertThat(project["ktfmtCheck"].dependencies).containsExactly(
            "ktfmtCheckAndroidTestDebugJavaSource",
            "ktfmtCheckAndroidTestReleaseJavaSource",
            "ktfmtCheckAndroidTestJavaSource",
            "ktfmtCheckTestDebugJavaSource",
            "ktfmtCheckTestReleaseJavaSource",
            "ktfmtCheckTestJavaSource",
            "ktfmtCheckDebugJavaSource",
            "ktfmtCheckReleaseJavaSource",
            "ktfmtCheckMainJavaSource"
        )
        assertThat(project["ktfmtFormat"].dependencies).containsExactly(
            "ktfmtFormatAndroidTestDebugJavaSource",
            "ktfmtFormatAndroidTestReleaseJavaSource",
            "ktfmtFormatAndroidTestJavaSource",
            "ktfmtFormatTestDebugJavaSource",
            "ktfmtFormatTestReleaseJavaSource",
            "ktfmtFormatTestJavaSource",
            "ktfmtFormatDebugJavaSource",
            "ktfmtFormatReleaseJavaSource",
            "ktfmtFormatMainJavaSource"
        )
    }

    private operator fun Project.get(value: String): Task = this.tasks.getByName(value)
    private val Task.dependencies: List<String>
        get() = this.dependsOn
            .filterIsInstance<TaskProvider<*>>()
            .map { it.name }
}
