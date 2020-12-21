package com.ncorti.ktfmt.gradle.util

import org.gradle.api.Project
import org.gradle.api.logging.Logger

class TestLogger(project: Project) : Logger by project.logger {

    val infoMessages = mutableListOf<String>()
    val warnMessages = mutableListOf<String>()
    val errorMessages = mutableListOf<String>()

    override fun info(message: String?) {
        message?.let { infoMessages.add(it) }
    }

    override fun warn(message: String?) {
        message?.let { warnMessages.add(it) }
    }

    override fun error(message: String?) {
        message?.let { errorMessages.add(it) }
    }

    fun reset() {
        infoMessages.clear()
        warnMessages.clear()
        errorMessages.clear()
    }
}
