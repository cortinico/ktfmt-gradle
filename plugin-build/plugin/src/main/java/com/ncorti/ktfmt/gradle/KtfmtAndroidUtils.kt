package com.ncorti.ktfmt.gradle

import com.android.build.api.dsl.CommonExtension
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.createTasksForSourceSet
import java.util.concurrent.Callable
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal object KtfmtAndroidUtils {

    internal fun applyKtfmtToAndroidProject(
        project: Project,
        topLevelFormat: TaskProvider<Task>,
        topLevelCheck: TaskProvider<Task>,
        ktfmtExtension: KtfmtExtension,
        isKmpProject: Boolean = false,
    ) {
        val marker = "ktfmt.android.tasks.created"

        val androidExtension = project.extensions.findByName("android") ?: return
        project.extensions.extraProperties.set(marker, true)

        fun createAndroidSourceSetTasks(name: String, srcDirs: Set<String>) {
            val sourceSetName = if (isKmpProject) "kmp $name" else name
            createTasksForSourceSet(
                project,
                sourceSetName,
                project.files(Callable { srcDirs }),
                ktfmtExtension,
                topLevelFormat,
                topLevelCheck,
            )
        }

        val ext = androidExtension as? CommonExtension ?: return
        ext.sourceSets.configureEach { sourceSet ->
            createAndroidSourceSetTasks(
                sourceSet.name,
                sourceSet.kotlin.directories + sourceSet.java.directories,
            )
        }
    }
}
