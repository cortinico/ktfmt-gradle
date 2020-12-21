plugins {
    kotlin("jvm")
    id("com.ncorti.ktfmt.gradle")
}

ktfmt {
    dropboxStyle()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(platform(TestingLib.JUNIT_BOM))
    testImplementation(TestingLib.JUPITER)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
