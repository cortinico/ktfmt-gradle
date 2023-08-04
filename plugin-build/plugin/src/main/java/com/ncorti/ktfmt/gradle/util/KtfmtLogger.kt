package com.ncorti.ktfmt.gradle.util

import org.gradle.api.logging.Logger

private const val TAG = "[ktfmt]"

internal fun Logger.i(message: String) = this.info("$TAG $message")

internal fun Logger.d(message: String) = this.debug("$TAG $message")

internal fun Logger.w(message: String, throwable: Throwable? = null) =
    if (throwable != null) {
        this.warn("$TAG $message", throwable)
    } else {
        this.warn("$TAG $message")
    }

internal fun Logger.e(message: String, throwable: Throwable? = null) =
    if (throwable != null) {
        this.error("$TAG $message", throwable)
    } else {
        this.error("$TAG $message")
    }
