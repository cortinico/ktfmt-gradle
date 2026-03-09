plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.ncorti.ktfmt.gradle")
}

kotlin {
    jvmToolchain(17)
    jvm()
}

ktfmt { kotlinLangStyle() }

tasks.withType<Test> { useJUnitPlatform() }
