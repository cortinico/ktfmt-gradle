plugins {
    kotlin("jvm")
    id("com.ncorti.ktfmt.gradle")
    id("com.google.devtools.ksp") version "1.9.0-1.0.11"
    id("app.cash.sqldelight") version "2.0.0-rc02"
}

ktfmt {
    kotlinLangStyle()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.jupiter)
    testRuntimeOnly(libs.jupiter.platform.launcher)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sqldelight {
  databases {
    create("Database") {
      packageName.set("com.example")
    }
  }
}
