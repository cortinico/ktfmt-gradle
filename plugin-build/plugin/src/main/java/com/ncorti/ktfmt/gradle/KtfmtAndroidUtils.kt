package com.ncorti.ktfmt.gradle

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.createTasksForSourceSet
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
        if (project.extensions.extraProperties.has(marker)) {
            return
        }

        val androidExtension = project.extensions.findByName("android") ?: return
        project.extensions.extraProperties.set(marker, true)

        fun createAndroidSourceSetTasks(name: String, srcDirs: Set<String>) {
            val sourceSetName = if (isKmpProject) "kmp $name" else name
            createTasksForSourceSet(
                project,
                sourceSetName,
                project.files(*srcDirs.toTypedArray()),
                ktfmtExtension,
                topLevelFormat,
                topLevelCheck,
            )
        }

        when (androidExtension) {
            is ApplicationExtension ->
                androidExtension.sourceSets.configureEach {
                    sourceSet: com.android.build.api.dsl.AndroidSourceSet ->
                    createAndroidSourceSetTasks(
                        sourceSet.name,
                        sourceSet.kotlin.directories + sourceSet.java.directories,
                    )
                }
            is LibraryExtension ->
                androidExtension.sourceSets.configureEach {
                    sourceSet: com.android.build.api.dsl.AndroidSourceSet ->
                    createAndroidSourceSetTasks(
                        sourceSet.name,
                        sourceSet.kotlin.directories + sourceSet.java.directories,
                    )
                }
            is TestExtension ->
                androidExtension.sourceSets.configureEach {
                    sourceSet: com.android.build.api.dsl.AndroidSourceSet ->
                    createAndroidSourceSetTasks(
                        sourceSet.name,
                        sourceSet.kotlin.directories + sourceSet.java.directories,
                    )
                }
            is DynamicFeatureExtension ->
                androidExtension.sourceSets.configureEach {
                    sourceSet: com.android.build.api.dsl.AndroidSourceSet ->
                    createAndroidSourceSetTasks(
                        sourceSet.name,
                        sourceSet.kotlin.directories + sourceSet.java.directories,
                    )
                }
            else -> Unit
        }
    }
}
