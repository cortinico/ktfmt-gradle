import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version BuildPluginsVersion.KOTLIN apply false
    id("com.gradle.plugin-publish") version BuildPluginsVersion.PLUGIN_PUBLISH apply false
    id("io.gitlab.arturbosch.detekt") version BuildPluginsVersion.DETEKT
    id("com.ncorti.ktfmt.gradle") version BuildPluginsVersion.KTFMT
    id("com.github.ben-manes.versions") version BuildPluginsVersion.VERSIONS_PLUGIN
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version BuildPluginsVersion.BINARY_COMPATIBILITY_VALIDATOR
}

allprojects {
    group = PluginCoordinates.GROUP
    version = PluginCoordinates.VERSION

    repositories {
        google()
        mavenCentral()
    }

    apply {
        plugin("io.gitlab.arturbosch.detekt")
        plugin("com.ncorti.ktfmt.gradle")
    }

    ktfmt {
        kotlinLangStyle()
    }

    detekt {
        config = rootProject.files("../config/detekt/detekt.yml")
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String) = "^[0-9,.v-]+(-r)?$".toRegex().matches(version).not()

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
