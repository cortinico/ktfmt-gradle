pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = ("ktfmt-gradle")

include(":example")
includeBuild("plugin-build")
