package com.ncorti.ktfmt.gradle

import com.ncorti.ktfmt.gradle.KtfmtAndroidUtils.applyKtfmtToAndroidProject
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.EXTENSION_NAME
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.TASK_NAME_CHECK
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.TASK_NAME_FORMAT
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.createTasksForSourceSet
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.registerGitHookTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask
import com.ncorti.ktfmt.gradle.util.i
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

/**
 * Top level class for the ktfmt-gradle plugin. Takes care of creating all the necessary classes
 * based on which other plugins are applied in the build.
 */
abstract class KtfmtPlugin : Plugin<Project> {

    private lateinit var ktfmtExtension: KtfmtExtension
    private lateinit var topLevelFormat: TaskProvider<Task>
    private lateinit var topLevelCheck: TaskProvider<Task>

    override fun apply(project: Project) =
        project.run {
            ktfmtExtension = project.extensions.create(EXTENSION_NAME, KtfmtExtension::class.java)

            // setup to pull in ktfmt separately to run on an isolated classloader
            val ktFmt =
                configurations.create("ktfmt").apply {
                    attributes.apply {
                        attribute(
                            Usage.USAGE_ATTRIBUTE,
                            project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME)
                        )
                    }
                    isVisible = false
                    isCanBeConsumed = false
                }

            val resourceUri =
                KtfmtPlugin::class.java.getResource("ktfmt-version.txt")
                    ?: error("Missing ktfmt version")
            val ktfmtVersion = resources.text.fromUri(resourceUri).asString()

            project.dependencies.add(ktFmt.name, ktfmtVersion)

            project.tasks.withType(KtfmtBaseTask::class.java).configureEach {
                it.ktfmtClasspath.from(ktFmt)
            }

            topLevelFormat = createTopLevelFormatTask(project)
            topLevelCheck = createTopLevelCheckTask(project)

            project.plugins.withId("kotlin") { applyKtfmt(project) }
            project.plugins.withId("kotlin-android") {
                if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                    project.logger.i("Skipping Android task creation, as KMP is applied")
                } else {
                    applyKtfmtToAndroidProject(
                        project,
                        ktfmtExtension,
                        topLevelFormat,
                        topLevelCheck
                    )
                }
            }
            project.plugins.withId("org.jetbrains.kotlin.js") { applyKtfmt(project) }
            project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
                applyKtfmtToMultiplatformProject(project)
            }
        }

    private fun applyKtfmt(project: Project) {
        val extension = project.extensions.getByType(KotlinProjectExtension::class.java)

        registerGitHookTask(project)

        extension.sourceSets.all {
            createTasksForSourceSet(
                project,
                it.name,
                it.kotlin.sourceDirectories,
                ktfmtExtension,
                topLevelFormat,
                topLevelCheck
            )
        }
    }

    private fun applyKtfmtToMultiplatformProject(project: Project) {
        val extension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        extension.sourceSets.all {
            val name = "kmp ${it.name}"
            if (it.name.startsWith("android")) {
                // We'll delegate Android Task Creation to the applyKtfmtToAndroidProject function
                // below
                return@all
            }
            createTasksForSourceSet(
                project,
                name,
                it.kotlin.sourceDirectories,
                ktfmtExtension,
                topLevelFormat,
                topLevelCheck
            )
        }

        extension.targets.all { kotlinTarget ->
            if (kotlinTarget.platformType == KotlinPlatformType.androidJvm) {
                applyKtfmtToAndroidProject(
                    project,
                    ktfmtExtension,
                    topLevelFormat,
                    topLevelCheck,
                    isKmpProject = true
                )
            }
        }
    }

    private fun createTopLevelFormatTask(project: Project): TaskProvider<Task> {
        return project.tasks.register(TASK_NAME_FORMAT) {
            it.group = "formatting"
            it.description = "Run Ktfmt formatter for all source sets for project '${project.name}'"
        }
    }

    private fun createTopLevelCheckTask(project: Project): TaskProvider<Task> {
        return project.tasks.register(TASK_NAME_CHECK) {
            it.group = "verification"
            it.description =
                "Run Ktfmt validation for all source sets for project '${project.name}'"
        }
    }

    companion object {
        internal val defaultExcludes = listOf("**/build/**")
        internal val defaultExcludesRegex = Regex("^(.*[\\\\/])?build([\\\\/].*)?\$")
        internal val defaultIncludes = listOf("**/*.kt", "**/*.kts")
    }
}
