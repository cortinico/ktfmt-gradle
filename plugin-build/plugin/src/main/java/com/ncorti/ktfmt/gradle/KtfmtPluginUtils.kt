package com.ncorti.ktfmt.gradle

import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import java.io.File
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal object KtfmtPluginUtils {

    internal const val EXTENSION_NAME = "ktfmt"

    internal const val TASK_NAME_FORMAT = "ktfmtFormat"

    internal const val TASK_NAME_CHECK = "ktfmtCheck"

    internal fun shouldCreateTasks(srcSetName: String): Boolean =
        when {
            // KSP is adding new source sets called `generatedByKspKotlin`,
            // `generatedByKspTestKotlin`
            // For those source sets we don't want to run KSP as they're only generated code.
            srcSetName.startsWith("generatedByKsp") -> false
            // Spring is adding sourceSets called `aot` and `aotTest` which we don't want to
            // inspect.
            srcSetName == "aot" || srcSetName == "aotTest" -> false
            else -> true
        }

    @Suppress("LongParameterList")
    internal fun createTasksForSourceSet(
        project: Project,
        srcSetName: String,
        srcSetDir: FileCollection,
        ktfmtExtension: KtfmtExtension,
        topLevelFormat: TaskProvider<Task>,
        topLevelCheck: TaskProvider<Task>,
    ) {
        if (shouldCreateTasks(srcSetName).not()) {
            return
        }

        val srcCheckTask = createCheckTask(project, srcSetName, srcSetDir, ktfmtExtension)
        val srcFormatTask = createFormatTask(project, srcSetName, srcSetDir, ktfmtExtension)

        // When running together with compileKotlin, ktfmt tasks should have precedence as
        // they're editing the source code
        project.tasks.withType(KotlinCompile::class.java).configureEach {
            it.mustRunAfter(srcCheckTask, srcFormatTask)
        }

        topLevelFormat.configure { task -> task.dependsOn(srcFormatTask) }
        topLevelCheck.configure { task -> task.dependsOn(srcCheckTask) }

        project.plugins.withType(LifecycleBasePlugin::class.java).configureEach {
            project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure { task ->
                task.dependsOn(srcCheckTask)
            }
        }
    }

    internal fun createScriptsTasks(
        project: Project,
        projectDir: File,
        topLevelFormat: TaskProvider<Task>,
        topLevelCheck: TaskProvider<Task>,
    ) {
        val checkTaskName = "${TASK_NAME_CHECK}Scripts"
        val formatTaskName = "${TASK_NAME_FORMAT}Scripts"

        val scriptFiles =
            project
                .fileTree(projectDir)
                .filter { it.extension == "kts" }
                .filter { it.parentFile == projectDir }

        val scriptCheckTask =
            project.tasks.register(checkTaskName, KtfmtCheckTask::class.java) {
                it.description =
                    "Run Ktfmt formatter validation for script files on project '${project.name}'"
                it.setSource(scriptFiles)
                it.setIncludes(KtfmtPlugin.defaultIncludes)
            }
        val scriptFormatTask =
            project.tasks.register(formatTaskName, KtfmtFormatTask::class.java) {
                it.description = "Run Ktfmt formatter for script files on project '${project.name}'"
                it.setSource(scriptFiles)
                it.setIncludes(KtfmtPlugin.defaultIncludes)
            }

        project.tasks.withType(KotlinCompile::class.java).configureEach {
            it.mustRunAfter(scriptCheckTask, scriptFormatTask)
        }

        topLevelFormat.configure { task -> task.dependsOn(scriptFormatTask) }
        topLevelCheck.configure { task -> task.dependsOn(scriptCheckTask) }

        project.plugins.withType(LifecycleBasePlugin::class.java).configureEach {
            project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure { task ->
                task.dependsOn(scriptCheckTask)
            }
        }
    }

    private fun createCheckTask(
        project: Project,
        name: String,
        srcDir: FileCollection,
        ktfmtExtension: KtfmtExtension,
    ): TaskProvider<KtfmtCheckTask> {
        val capitalizedName =
            name.split(" ").joinToString("") {
                val charArray = it.toCharArray()
                if (charArray[0].isLowerCase()) {
                    charArray[0] = charArray[0].uppercaseChar()
                }
                charArray.concatToString()
            }
        val taskName = "$TASK_NAME_CHECK$capitalizedName"

        val inputDirs = project.getSelectedSrcSets(srcDir, ktfmtExtension)
        return project.tasks.register(taskName, KtfmtCheckTask::class.java) {
            it.description =
                "Run Ktfmt formatter validation for sourceSet '$name' on project '${project.name}'"
            it.setSource(inputDirs)
            it.setIncludes(KtfmtPlugin.defaultIncludes)
        }
    }

    private fun createFormatTask(
        project: Project,
        name: String,
        srcDir: FileCollection,
        ktfmtExtension: KtfmtExtension,
    ): TaskProvider<KtfmtFormatTask> {
        val srcSetName =
            name.split(" ").joinToString("") {
                val charArray = it.toCharArray()
                if (charArray[0].isLowerCase()) {
                    charArray[0] = charArray[0].uppercaseChar()
                }
                charArray.concatToString()
            }
        val taskName = "$TASK_NAME_FORMAT$srcSetName"

        val inputDirs = project.getSelectedSrcSets(srcDir, ktfmtExtension)
        return project.tasks.register(taskName, KtfmtFormatTask::class.java) {
            it.description =
                "Run Ktfmt formatter for sourceSet '$name' on project '${project.name}'"
            it.setSource(inputDirs)
            it.setIncludes(KtfmtPlugin.defaultIncludes)
        }
    }

    private fun Project.getSelectedSrcSets(
        srcDir: FileCollection,
        ktfmtExtension: KtfmtExtension,
    ): Provider<List<File>> {
        val excludedSourceSets = ktfmtExtension.srcSetPathExclusionPattern

        return provider {
            srcDir.toList().filterNot { it.absolutePath.matches(excludedSourceSets.get()) }
        }
    }
}
