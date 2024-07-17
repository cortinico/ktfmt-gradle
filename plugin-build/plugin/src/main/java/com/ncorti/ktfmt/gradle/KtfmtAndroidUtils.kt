package com.ncorti.ktfmt.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.createTasksForSourceSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

internal object KtfmtAndroidUtils {

    internal fun applyKtfmtToAndroidProject(
        project: Project,
        ktfmtExtension: KtfmtExtension,
        topLevelFormat: TaskProvider<Task>,
        topLevelCheck: TaskProvider<Task>,
        isKmpProject: Boolean = false
    ) {

        fun applyKtfmtForAndroid() {
            project.extensions.configure(BaseExtension::class.java) {
                it.sourceSets.all { sourceSet ->
                    val sourceSetName =
                        if (isKmpProject) {
                            "kmp ${sourceSet.name}"
                        } else {
                            sourceSet.name
                        }

                    val srcDirs = sourceSet.fileCollectionProvider(project)
                    // Passing Callable, so returned FileCollection, will lazy evaluate it
                    // only when task will need it.
                    // Solves the problem of having additional source dirs in
                    // current AndroidSourceSet, that are not available on eager
                    // evaluation.
                    createTasksForSourceSet(
                        project,
                        sourceSetName,
                        srcDirs,
                        ktfmtExtension,
                        topLevelFormat,
                        topLevelCheck,
                    )
                }
            }
        }

        project.plugins.withId("com.android.application") { applyKtfmtForAndroid() }
        project.plugins.withId("com.android.library") { applyKtfmtForAndroid() }
        project.plugins.withId("com.android.test") { applyKtfmtForAndroid() }
        project.plugins.withId("com.android.dynamic-feature") { applyKtfmtForAndroid() }
    }

    @Suppress("DEPRECATION")
    private fun AndroidSourceSet.fileCollectionProvider(
        project: Project
    ): Provider<FileCollection> {
        val objectFactory = project.objects

        return project.provider {
            val kotlinSrcFiles =
                runCatching {
                        // As sourceSet.kotlin doesn't exist before AGP 7
                        (kotlin as? DefaultAndroidSourceDirectorySet)?.srcDirs
                    }
                    .getOrNull()
                    .orEmpty()

            val files = java.srcDirs + kotlinSrcFiles

            objectFactory.fileCollection().from(files)
        }
    }
}
