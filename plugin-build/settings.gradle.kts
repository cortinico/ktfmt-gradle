pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "binary-compatibility-validator") {
                useModule("org.jetbrains.kotlinx:binary-compatibility-validator:${requested.version}")
            }
        }
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
}
rootProject.name = ("com.ncorti.ktfmt.gradle")

include(":plugin")
