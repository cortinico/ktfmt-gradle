import app.cash.sqldelight.gradle.VerifyMigrationTask

plugins {
    kotlin("jvm")
    id("com.ncorti.ktfmt.gradle")
    id("com.google.devtools.ksp") version "2.1.20-2.0.0"
    id("app.cash.sqldelight") version "2.0.2"
}

ktfmt { kotlinLangStyle() }

kotlin { jvmToolchain(17) }

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.jupiter)
    testRuntimeOnly(libs.jupiter.platform.launcher)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<VerifyMigrationTask> { enabled = false }

sqldelight { databases { create("Database") { packageName.set("com.example") } } }
