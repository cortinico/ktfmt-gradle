plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    id("com.ncorti.ktfmt.gradle")
}

kotlin {
    jvmToolchain(17)
    jvm()

    androidLibrary {
        namespace = "com.ncorti.example.ktfmt"
        compileSdk = 33
        minSdk = 33

        withHostTest {}
    }

    sourceSets { commonTest { dependencies { implementation(kotlin("test")) } } }
}

ktfmt { kotlinLangStyle() }

tasks.withType<Test> { useJUnitPlatform() }
