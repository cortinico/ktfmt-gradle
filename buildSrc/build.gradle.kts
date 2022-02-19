import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    id("com.ncorti.ktfmt.gradle") version "0.7.0"
}

repositories {
    mavenCentral()
}

ktfmt {
    kotlinLangStyle()
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}