package com.ncorti.ktfmt.gradle

import com.ncorti.ktfmt.gradle.GradleWorkerIsolationStrategy.*
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

enum class GradleWorkerIsolationStrategy {
    NO_ISOLATION,
    PROCESS_ISOLATION
}

fun WorkerExecutor.getWorkQueue(
    isolationStrategy: GradleWorkerIsolationStrategy,
    classPath: ConfigurableFileCollection
): WorkQueue {
    return when (isolationStrategy) {
        NO_ISOLATION -> this.noIsolation()
        PROCESS_ISOLATION -> this.processIsolation { it.classpath.from(classPath) }
    }
}
