package com.ncorti.ktfmt.gradle.testutil

import java.io.File
import org.intellij.lang.annotations.Language

fun File.appendToBuildGradle(content: String) {
    resolve("build.gradle.kts").apply {
        appendText(System.lineSeparator())
        appendText(content)
        appendText(System.lineSeparator())
    }
}

fun File.createTempFile(
    @Language("kotlin") content: String,
    fileName: String = "TestFile.kt",
    path: String = "src/main/java",
): File =
    resolve(path).resolve(fileName).apply {
        parentFile.mkdirs()
        createNewFile()
        writeText(content)
    }
