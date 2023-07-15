import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.pluginPublish) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.versionCheck)
    alias(libs.plugins.binaryCompatibilityValidator)
}

allprojects {
    group = property("GROUP").toString()
    version = property("VERSION").toString()

    apply {
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
        plugin(rootProject.libs.plugins.ktfmt.get().pluginId)
    }

    ktfmt {
        kotlinLangStyle()
    }

    detekt {
        config.setFrom(rootProject.files("../config/detekt/detekt.yml"))
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
