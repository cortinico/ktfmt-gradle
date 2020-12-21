pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
}

rootProject.name = ("ktfmt-gradle")

include(":example")
includeBuild("plugin-build")
