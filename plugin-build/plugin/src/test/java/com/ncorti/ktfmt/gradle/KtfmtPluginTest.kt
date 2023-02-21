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

        assertThat(project["ktfmtCheck"].dependencies)
            .containsExactly("ktfmtCheckMain", "ktfmtCheckTest")
        assertThat(project["ktfmtFormat"].dependencies)
            .containsExactly("ktfmtFormatMain", "ktfmtFormatTest")
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

        assertThat(project["ktfmtCheck"].dependencies)
            .containsExactly("ktfmtCheckMain", "ktfmtCheckTest")
        assertThat(project["ktfmtFormat"].dependencies)
            .containsExactly("ktfmtFormatMain", "ktfmtFormatTest")
    }

    @Test
    fun `plugin is applied correctly to a kotlin multiplatform project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        assertThat(project.tasks.getByName("ktfmtCheckKmpCommonMain"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatKmpCommonMain"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckKmpCommonTest"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatKmpCommonTest"))
            .isInstanceOf(KtfmtFormatTask::class.java)

        assertThat(project["ktfmtCheck"].dependencies)
            .containsExactly("ktfmtCheckKmpCommonMain", "ktfmtCheckKmpCommonTest")
        assertThat(project["ktfmtFormat"].dependencies)
            .containsExactly("ktfmtFormatKmpCommonMain", "ktfmtFormatKmpCommonTest")
    }

    @Suppress("LongMethod")
    @Test
    fun `plugin is applied correctly to an android application project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.android.application")
        project.pluginManager.apply("org.jetbrains.kotlin.android")
        project.pluginManager.apply("com.ncorti.ktfmt.gradle")

        assertDoesNotThrow {
            project.tasks.getByName("ktfmtCheck")
            project.tasks.getByName("ktfmtFormat")
        }

        // AndroidTest
        assertThat(project.tasks.getByName("ktfmtCheckAndroidTestDebug"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckAndroidTestRelease"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckAndroidTest"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatAndroidTestDebug"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatAndroidTestRelease"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatAndroidTest"))
            .isInstanceOf(KtfmtFormatTask::class.java)

        // Test
        assertThat(project.tasks.getByName("ktfmtCheckTestDebug"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckTestRelease"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckTest"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestDebug"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestRelease"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTest"))
            .isInstanceOf(KtfmtFormatTask::class.java)

        // Prod
        assertThat(project.tasks.getByName("ktfmtCheckDebug"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckRelease"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckMain"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatDebug"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatRelease"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatMain"))
            .isInstanceOf(KtfmtFormatTask::class.java)

        assertThat(project.tasks.getByName("ktfmtCheckTestFixtures"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckTestFixturesRelease"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtCheckTestFixturesDebug"))
            .isInstanceOf(KtfmtCheckTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestFixtures"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestFixturesRelease"))
            .isInstanceOf(KtfmtFormatTask::class.java)
        assertThat(project.tasks.getByName("ktfmtFormatTestFixturesDebug"))
            .isInstanceOf(KtfmtFormatTask::class.java)

        assertThat(project["ktfmtCheck"].dependencies)
            .containsExactly(
                "ktfmtCheckAndroidTestDebug",
                "ktfmtCheckAndroidTestRelease",
                "ktfmtCheckAndroidTest",
                "ktfmtCheckTestDebug",
                "ktfmtCheckTestRelease",
                "ktfmtCheckTest",
                "ktfmtCheckDebug",
                "ktfmtCheckRelease",
                "ktfmtCheckMain",
                "ktfmtCheckTestFixtures",
                "ktfmtCheckTestFixturesRelease",
                "ktfmtCheckTestFixturesDebug"
            )
        assertThat(project["ktfmtFormat"].dependencies)
            .containsExactly(
                "ktfmtFormatAndroidTestDebug",
                "ktfmtFormatAndroidTestRelease",
                "ktfmtFormatAndroidTest",
                "ktfmtFormatTestDebug",
                "ktfmtFormatTestRelease",
                "ktfmtFormatTest",
                "ktfmtFormatDebug",
                "ktfmtFormatRelease",
                "ktfmtFormatMain",
                "ktfmtFormatTestFixtures",
                "ktfmtFormatTestFixturesRelease",
                "ktfmtFormatTestFixturesDebug"
            )
    }

    @Suppress("UnusedPrivateMember")
    private operator fun Project.get(value: String): Task = this.tasks.getByName(value)
    private val Task.dependencies: List<String>
        get() = this.dependsOn.filterIsInstance<TaskProvider<*>>().map { it.name }
}
