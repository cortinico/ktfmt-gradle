package com.ncorti.ktfmt.gradle

import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal object KtfmtPluginUtils {

    internal const val EXTENSION_NAME = "ktfmt"

    internal const val TASK_NAME_FORMAT = "ktfmtFormat"

    internal const val TASK_NAME_CHECK = "ktfmtCheck"

    @Suppress("LongParameterList")
    internal fun createTasksForSourceSet(
        project: Project,
        srcSetName: String,
        srcSetDir: FileCollection,
        ktfmtExtension: KtfmtExtension,
        topLevelFormat: TaskProvider<Task>,
        topLevelCheck: TaskProvider<Task>,
        visitedSourceSets : MutableSet<VisitedSourceSets>
    ) {
        val visited = VisitedSourceSets(srcSetName, project)
        if (visitedSourceSets.contains(visited)) {
            return
        }
        visitedSourceSets.add(visited)
        val srcCheckTask = createCheckTask(project, ktfmtExtension, srcSetName, srcSetDir)
        val srcFormatTask = createFormatTask(project, ktfmtExtension, srcSetName, srcSetDir)

        // When running together with compileKotlin, ktfmt tasks should have precedence as
        // they're editing the source code
        project.tasks.withType(KotlinCompile::class.java).all {
            it.mustRunAfter(srcCheckTask, srcFormatTask)
        }

        topLevelFormat.configure { task -> task.dependsOn(srcFormatTask) }
        topLevelCheck.configure { task -> task.dependsOn(srcCheckTask) }

        project.plugins.withType(LifecycleBasePlugin::class.java) {
            project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure { task ->
                task.dependsOn(srcCheckTask)
            }
        }
    }

    private fun createCheckTask(
        project: Project,
        ktfmtExtension: KtfmtExtension,
        name: String,
        srcDir: FileCollection
    ): TaskProvider<KtfmtCheckTask> {
        val capitalizedName =
            name.split(" ").joinToString("") {
                val charArray = it.toCharArray()
                if (charArray[0].isLowerCase()) {
                    // We use toUpperCase here to retain compatibility with Gradle 6.9 and Kotlin
                    // 1.4
                    @Suppress("DEPRECATION")
                    charArray[0] = charArray[0].toUpperCase()
                }
                charArray.concatToString()
            }
        val taskName = "$TASK_NAME_CHECK$capitalizedName"
        return project.tasks.register(taskName, KtfmtCheckTask::class.java) {
            it.description =
                "Run Ktfmt formatter for sourceSet '$name' on project '${project.name}'"
            it.setSource(srcDir)
            it.setIncludes(KtfmtPlugin.defaultIncludes)
            it.setExcludes(KtfmtPlugin.defaultExcludes)
            it.bean = ktfmtExtension.toBean()
        }
    }

    private fun createFormatTask(
        project: Project,
        ktfmtExtension: KtfmtExtension,
        name: String,
        srcDir: FileCollection
    ): TaskProvider<KtfmtFormatTask> {
        val srcSetName =
            name.split(" ").joinToString("") {
                val charArray = it.toCharArray()
                if (charArray[0].isLowerCase()) {
                    // We use toUpperCase here to retain compatibility with Gradle 6.9 and Kotlin
                    // 1.4
                    @Suppress("DEPRECATION")
                    charArray[0] = charArray[0].toUpperCase()
                }
                charArray.concatToString()
            }
        val taskName = "$TASK_NAME_FORMAT$srcSetName"
        return project.tasks.register(taskName, KtfmtFormatTask::class.java) {
            it.description =
                "Run Ktfmt formatter validation for sourceSet '$name' on project '${project.name}'"
            it.setSource(srcDir)
            it.setIncludes(KtfmtPlugin.defaultIncludes)
            it.setExcludes(KtfmtPlugin.defaultExcludes)
            it.bean = ktfmtExtension.toBean()
        }
    }
}
