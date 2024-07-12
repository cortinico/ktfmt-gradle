plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.pluginPublish) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.binaryCompatibilityValidator)
}

allprojects {
    group = property("GROUP").toString()
    version = property("VERSION").toString()

    apply {
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
        plugin(rootProject.libs.plugins.ktfmt.get().pluginId)
    }

    ktfmt { kotlinLangStyle() }

    detekt { config.setFrom(rootProject.files("../config/detekt/detekt.yml")) }
}

tasks.register<Delete>("clean") { delete(layout.buildDirectory) }
