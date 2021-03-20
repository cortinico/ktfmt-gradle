object PluginCoordinates {
    const val ID = "com.ncorti.ktfmt.gradle"
    const val GROUP = "com.ncorti.ktfmt.gradle"
    const val VERSION = "0.5.0"
    const val IMPLEMENTATION_CLASS = "com.ncorti.ktfmt.gradle.KtfmtPlugin"
}

object PluginBundle {
    const val VCS = "https://github.com/cortinico/ktfmt-gradle"
    const val WEBSITE = "https://github.com/cortinico/ktfmt-gradle"
    const val DESCRIPTION = "A Gradle plugin to run ktfmt formatter on your build"
    const val DISPLAY_NAME = "ktfmt-gradle"

    val TAGS = listOf(
        "ktfmt",
        "kotlin",
        "formatter",
        "reformat",
        "style",
        "code",
        "linter",
        "plugin",
        "gradle"
    )
}

