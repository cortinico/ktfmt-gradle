package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.tasks.worker.PreCommitHookFormatAction
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor

/** ktfmt-gradle Git hook task. Creates a git hook that runs ktfmt on git commit */
abstract class CreateGitPreCommitHookTask : DefaultTask() {
    @Inject abstract fun getWorkerExecutor(): WorkerExecutor

    @TaskAction
    fun createHook() {
        val workQueue = getWorkerExecutor().noIsolation()
        workQueue.submit(PreCommitHookFormatAction::class.java) { parameters ->
            parameters.projectDir.set(project.projectDir)
        }
    }
}
