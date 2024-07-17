plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    apply { plugin(rootProject.libs.plugins.detekt.get().pluginId) }

    detekt { config.setFrom(rootProject.files("config/detekt/detekt.yml")) }
}

tasks.register<Delete>("clean") { delete(layout.buildDirectory) }

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
