package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.tasks.worker.PreCommitHookFormatAction
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import org.gradle.workers.WorkerExecutor

/** ktfmt-gradle Git hook task. Creates a git hook that runs ktfmt on git commit */
abstract class CreateGitPreCommitHookTask : SourceTask() {
    @Inject abstract fun getWorkerExecutor(): WorkerExecutor

    @get:InputDirectory
    @get:Incremental
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectDir: DirectoryProperty

    @TaskAction
    fun createHook() {
        val workQueue = getWorkerExecutor().noIsolation()
        workQueue.submit(PreCommitHookFormatAction::class.java) { parameters ->
            parameters.projectDir.set(projectDir)
        }
    }
}
