pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = ("ktfmt-gradle")

include(
    ":example",
    ":example-kmp",
)

includeBuild(
    "plugin-build",
)
