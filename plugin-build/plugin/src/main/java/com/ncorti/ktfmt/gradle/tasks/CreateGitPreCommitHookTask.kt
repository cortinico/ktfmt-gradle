package com.ncorti.ktfmt.gradle.tasks

import com.ncorti.ktfmt.gradle.util.e
import com.ncorti.ktfmt.gradle.util.i
import com.ncorti.ktfmt.gradle.util.w
import java.io.File
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.jetbrains.kotlin.incremental.createDirectory

/** ktfmt-gradle Git hook task. Creates a git hook that runs ktfmt on git commit */
abstract class CreateGitPreCommitHookTask : SourceTask() {
    @Inject abstract fun getWorkerExecutor(): WorkerExecutor

    @get:InputDirectory abstract val projectDir: DirectoryProperty

    @TaskAction
    fun createHook() {
        val workQueue = getWorkerExecutor().noIsolation()
        workQueue.submit(PreCommitHookFormatAction::class.java) { parameters ->
            parameters.projectDir.set(projectDir)
        }
    }
}

internal abstract class PreCommitHookFormatAction :
    WorkAction<PreCommitHookFormatAction.PreCommitHooParameters> {

    interface PreCommitHooParameters : WorkParameters {
        val projectDir: DirectoryProperty
    }

    protected val logger: Logger = Logging.getLogger(PreCommitHookFormatAction::class.java)

    override fun execute() {
        val rootDirectory = parameters.projectDir.get().asFile
        val gitDirectory = File("${rootDirectory.path}/.git")
        val hooksDirectory = File("${gitDirectory.path}/hooks")

        if (!gitDirectory.exists()) {
            logger.e("Git directory not found, will not create the hook")
            return
        }

        if (!hooksDirectory.exists()) {
            logger.w("Hooks directory does not exist, will create one")
            hooksDirectory.createDirectory()
        }

        val hookFile = File("${hooksDirectory}/pre-commit")

        if (hookFile.exists()) {
            logger.w("Pre commit hook already exists. Ktfmt pre hook generation aborted")
        } else {
            hookFile.createNewFile()

            hookFile.writeText(
                """
                #!/bin/bash
                staged_files_count=${'$'}(git diff --name-only --cached --numstat -- '*.kt' | wc -l)
                        
                # format only if there are kotlin files in git's index
                if [[ ${'$'}staged_files_count -gt 0 ]]; then
                    ${rootDirectory.path}/gradlew ktfmtPrecommitHook --include-only $(git diff --name-only --cached -- '*.kt' | paste -sd ",");
                    git add -A $(git diff --name-only --cached -- '*.kt')
                fi
                exit 0;
            """
                    .trimIndent()
            )

            logger.i("Ktfmt pre commit hook was created")
        }
    }
}
