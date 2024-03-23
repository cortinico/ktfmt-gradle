package com.ncorti.ktfmt.gradle.tasks.worker

import com.ncorti.ktfmt.gradle.util.e
import com.ncorti.ktfmt.gradle.util.i
import com.ncorti.ktfmt.gradle.util.w
import java.io.File
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jetbrains.kotlin.incremental.createDirectory

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
            logger.i("Hooks directory does not exist, will create one")
            hooksDirectory.createDirectory()
        }

        val hookFile = File("${hooksDirectory}/pre-commit")

        if (hookFile.exists()) {
            logger.w("Pre commit hook file (${hookFile.path}) already exists, aborting hook generation")
        } else {
            hookFile.createNewFile()

            hookFile.writeText(
                """
                #!/bin/bash
                staged_files_count=${'$'}(git diff --name-only --cached --numstat -- '*.kt' | wc -l)
                        
                # format only if there are kotlin files in git's index
                if [[ ${'$'}staged_files_count -gt 0 ]]; then
                    ${rootDirectory.path}/gradlew ktfmtPreCommitHook --include-only $(git diff --name-only --cached -- '*.kt' | paste -sd ",");
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
