plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id("com.ncorti.ktfmt.gradle")
}

kotlin {
    jvmToolchain(17)
    jvm()
    androidTarget()
}

ktfmt {
    kotlinLangStyle()
}

android {
    namespace = "com.ncorti.example.ktfmt"
    compileSdk = 33
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.jupiter)
}
