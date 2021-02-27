object Versions {
    const val AGP = "4.1.2"
    const val COROUTINES = "1.4.2"
    const val DIFF_UTIL = "4.9"
    const val JUPITER = "5.7.1"
    const val KTFMT = "0.21"
    const val TRUTH = "1.1.2"
}

object BuildPluginsVersion {
    const val BINARY_COMPATIBILITY_VALIDATOR = "0.4.0"
    const val DETEKT = "1.15.0"
    const val KOTLIN = "1.4.21"
    const val KTFMT = "0.4.0"
    const val PLUGIN_PUBLISH = "0.13.0"
    const val VERSIONS_PLUGIN = "0.36.0"
}

object Libs {
    const val AGP = "com.android.tools.build:gradle:${Versions.AGP}"
    const val COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}"
    const val DIFF_UTILS = "io.github.java-diff-utils:java-diff-utils:${Versions.DIFF_UTIL}"
    const val KTFMT = "com.facebook:ktfmt:${Versions.KTFMT}"
}

object TestingLib {
    const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}"
    const val JUNIT_BOM = "org.junit:junit-bom:${Versions.JUPITER}"
    const val JUPITER = "org.junit.jupiter:junit-jupiter"
    const val TRUTH = "com.google.truth:truth:${Versions.TRUTH}"
}