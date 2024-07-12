import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.versionCheck)
}

subprojects {
    apply { plugin(rootProject.libs.plugins.detekt.get().pluginId) }

    detekt { config.setFrom(rootProject.files("config/detekt/detekt.yml")) }
}

tasks.withType<DependencyUpdatesTask> { rejectVersionIf { isNonStable(candidate.version) } }

fun isNonStable(version: String) = "^[0-9,.v-]+(-r)?$".toRegex().matches(version).not()

tasks.register("clean", Delete::class.java) { delete(rootProject.buildDir) }

tasks.register("reformatAll") {
    description = "Reformat all the Kotlin Code"

    dependsOn(":example:ktfmtFormat")
    dependsOn(gradle.includedBuild("plugin-build").task(":plugin:ktfmtFormat"))
}

tasks.register("preMerge") {
    description = "Runs all the tests/verification tasks on both top level and included build."

    dependsOn(":example:check")
    dependsOn(gradle.includedBuild("plugin-build").task(":plugin:check"))
    dependsOn(gradle.includedBuild("plugin-build").task(":plugin:validatePlugins"))
}
