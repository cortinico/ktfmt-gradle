import app.cash.sqldelight.gradle.SqlDelightWorkerTask
import app.cash.sqldelight.gradle.VerifyMigrationTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask

plugins {
    kotlin("jvm")
    id("com.ncorti.ktfmt.gradle")
    id("com.google.devtools.ksp") version "2.0.0-1.0.23"
    id("app.cash.sqldelight") version "2.0.2"
}

ktfmt { kotlinLangStyle() }

kotlin { jvmToolchain(17) }

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.jupiter)
    testRuntimeOnly(libs.jupiter.platform.launcher)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<VerifyMigrationTask> { enabled = false }

tasks.withType<KtfmtBaseTask>(){
    mustRunAfter(tasks.withType<SqlDelightWorkerTask>())
}


sqldelight { databases { create("Database") { packageName.set("com.example") } } }

