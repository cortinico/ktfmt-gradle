package com.ncorti.ktfmt.gradle.testutil

fun String.ensureLineSeparator(): String = replace(Regex("\r|\n|\r\n}"), System.lineSeparator())
