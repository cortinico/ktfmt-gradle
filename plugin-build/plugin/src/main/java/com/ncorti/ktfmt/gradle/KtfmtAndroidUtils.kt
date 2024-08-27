package com.ncorti.ktfmt.gradle

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
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
        fun applyKtfmtForAndroid() {
            project.extensions.configure(BaseExtension::class.java) {
                it.sourceSets.all { sourceSet ->
                    @Suppress("DEPRECATION")
                    val sourceSetName =
                        if (isKmpProject) {
                            "kmp ${sourceSet.name}"
                        } else {
                            sourceSet.name
                        }
                    val srcDirs =
                        sourceSet.java.srcDirs +
                            runCatching {
                                    // As sourceSet.kotlin doesn't exist before AGP 7
                                    (sourceSet.kotlin as? DefaultAndroidSourceDirectorySet)?.srcDirs
                                }
                                .getOrNull()
                                .orEmpty()
                    // Passing Callable, so returned FileCollection, will lazy evaluate it
                    // only when task will need it.
                    // Solves the problem of having additional source dirs in
                    // current AndroidSourceSet, that are not available on eager
                    // evaluation.
                    createTasksForSourceSet(
                        project,
                        sourceSetName,
                        project.files(Callable { srcDirs }),
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
}
