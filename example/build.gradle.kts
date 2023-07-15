plugins {
    kotlin("jvm")
    id("com.ncorti.ktfmt.gradle")
}

ktfmt {
    kotlinLangStyle()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.jupiter)
    testRuntimeOnly(libs.jupiter.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
