plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    id("com.ncorti.ktfmt.gradle")
}

kotlin {
    jvmToolchain(17)
    jvm()

    ktfmt { kotlinLangStyle() }

    android {
        namespace = "com.ncorti.example.ktfmt"
        compileSdk = 33
        withHostTest {}
    }

    sourceSets {
        jvmTest {
            dependencies {
                implementation(project.dependencies.platform(libs.junit.bom))
                implementation(libs.jupiter)
                runtimeOnly(libs.jupiter.platform.launcher)
            }
        }
    }
}

tasks.withType<Test> { useJUnitPlatform() }
