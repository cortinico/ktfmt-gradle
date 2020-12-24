package com.ncorti.ktfmt.gradle

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.dsl.SigningConfig
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantProperties
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import java.util.concurrent.Callable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

private const val EXTENSION_NAME = "ktfmt"

private const val TASK_NAME_FORMAT = "ktfmtFormat"

private const val TASK_NAME_CHECK = "ktfmtCheck"

/**
 * Top level class for the ktfmt-gradle plugin. Takes care of creating all the necessary classes
 * based on which other plugins are applied in the build.
 */
abstract class KtfmtPlugin : Plugin<Project> {

    private lateinit var ktfmtExtension: KtfmtExtension
    private lateinit var topLevelFormat: TaskProvider<Task>
    private lateinit var topLevelCheck: TaskProvider<Task>

    override fun apply(project: Project) {
        ktfmtExtension =
            project.extensions.create(EXTENSION_NAME, KtfmtExtension::class.java, project)

        topLevelFormat = createTopLevelFormatTask(project)
        topLevelCheck = createTopLevelCheckTask(project)

        project.plugins.withId("kotlin") { applyKtfmt(project) }
        project.plugins.withId("kotlin-android") { applyKtfmtToAndroidProject(project) }
        project.plugins.withId("org.jetbrains.kotlin.js") { applyKtfmt(project) }
        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            applyKtfmtToMultiplatformProject(project)
        }
    }

    private fun applyKtfmt(project: Project) {
        val extension = project.extensions.getByType(KotlinProjectExtension::class.java)
        extension.sourceSets.all {
            createTasksForSourceSet(project, it.name, it.kotlin.sourceDirectories)
        }
    }

    private fun applyKtfmtToMultiplatformProject(project: Project) {
        val extension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        extension.sourceSets.all {
            createTasksForSourceSet(project, it.name, it.kotlin.sourceDirectories)
        }

        extension.targets.all { kotlinTarget ->
            if (kotlinTarget.platformType == KotlinPlatformType.androidJvm) {
                applyKtfmtToAndroidProject(project)
            }
        }
    }

    private fun applyKtfmtToAndroidProject(project: Project) {
        project.plugins.withId("com.android.application") { applyKtfmtToAndroidPlugin(project) }
        project.plugins.withId("com.android.library") { applyKtfmtToAndroidPlugin(project) }
        project.plugins.withId("com.android.test") { applyKtfmtToAndroidPlugin(project) }
        project.plugins.withId("com.android.dynamic-feature") { applyKtfmtToAndroidPlugin(project) }
    }

    @Suppress("UnstableApiUsage", "UNCHECKED_CAST")
    private fun applyKtfmtToAndroidPlugin(project: Project) {
        project.extensions.configure(CommonExtension::class.java) { ext ->
            val androidCommonExtension = ext as AndroidCommonExtension

            androidCommonExtension.sourceSets.all { sourceSet ->
                // https://issuetracker.google.com/u/1/issues/170650362
                val androidSourceSet = sourceSet.java as DefaultAndroidSourceDirectorySet
                // Passing Callable, so returned FileCollection, will lazy evaluate it
                // only when task will need it.
                // Solves the problem of having additional source dirs in
                // current AndroidSourceSet, that are not available on eager
                // evaluation.
                createTasksForSourceSet(
                    project,
                    androidSourceSet.name,
                    project.files(Callable { androidSourceSet.srcDirs }))
            }
        }
    }

    private fun createTasksForSourceSet(
        project: Project,
        srcSetName: String,
        srcSetDir: FileCollection
    ) {
        val srcCheckTask = createCheckTask(project, ktfmtExtension, srcSetName, srcSetDir)
        val srcFormatTask = createFormatTask(project, ktfmtExtension, srcSetName, srcSetDir)
        topLevelFormat.configure { task -> task.dependsOn(srcFormatTask) }
        topLevelCheck.configure { task -> task.dependsOn(srcCheckTask) }

        project.plugins.withType(LifecycleBasePlugin::class.java) {
            project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure { task ->
                task.dependsOn(srcCheckTask)
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

    private fun createCheckTask(
        project: Project,
        ktfmtExtension: KtfmtExtension,
        name: String,
        srcDir: FileCollection
    ): TaskProvider<KtfmtCheckTask> {
        val capitalizedName = name.split(" ").joinToString("") { it.capitalize() }
        val taskName = "$TASK_NAME_CHECK$capitalizedName"
        return project.tasks.register(taskName, KtfmtCheckTask::class.java) {
            it.description =
                "Run Ktfmt formatter for sourceSet '$name' on project '${project.name}'"
            it.setSource(srcDir)
            it.setIncludes(defaultIncludes)
            it.setExcludes(defaultExcludes)
            it.bean = ktfmtExtension.toBean()
        }
    }

    private fun createFormatTask(
        project: Project,
        ktfmtExtension: KtfmtExtension,
        name: String,
        srcDir: FileCollection
    ): TaskProvider<KtfmtFormatTask> {
        val srcSetName = name.split(" ").joinToString("") { it.capitalize() }
        val taskName = "$TASK_NAME_FORMAT$srcSetName"
        return project.tasks.register(taskName, KtfmtFormatTask::class.java) {
            it.description =
                "Run Ktfmt formatter validation for sourceSet '$name' on project '${project.name}'"
            it.setSource(srcDir)
            it.setIncludes(defaultIncludes)
            it.setExcludes(defaultExcludes)
            it.bean = ktfmtExtension.toBean()
        }
    }

    companion object {
        internal val defaultExcludes = listOf("build/")
        internal val defaultIncludes = listOf("**/*.kt", "**/*.kts")
    }
}

@Suppress("UnstableApiUsage")
private typealias AndroidCommonExtension =
    CommonExtension<
        AndroidSourceSet,
        BuildFeatures,
        BuildType,
        DefaultConfig,
        ProductFlavor,
        SigningConfig,
        Variant<VariantProperties>,
        VariantProperties>
