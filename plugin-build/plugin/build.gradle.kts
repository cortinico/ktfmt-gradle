import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("signing")
    alias(libs.plugins.pluginPublish)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.fromVersion("1.6"))
        languageVersion.set(KotlinVersion.fromVersion("1.6"))
        jvmTarget = JvmTarget.JVM_11
    }
}

/**
 * We create a configuration that we can resolve by extending [compileOnly]. Here we inject the
 * dependencies into TestKit plugin classpath via [PluginUnderTestMetadata] to make them available
 * for testing.
 */
val integrationTestRuntime: Configuration by configurations.creating

integrationTestRuntime.apply {
    extendsFrom(configurations.getByName("compileOnly"))
    attributes {
        attribute(Attribute.of("org.gradle.usage", String::class.java), "java-runtime")
        attribute(Attribute.of("org.gradle.category", String::class.java), "library")
    }
}

tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(integrationTestRuntime)
}

dependencies {
    compileOnly(libs.ktfmt){
//        isTransitive = false
        exclude("org.jetbrains.kotlin")
    }
    implementation(libs.diffUtils)

    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(libs.agp)

    testImplementation(kotlin("gradle-plugin"))
    testImplementation(libs.agp)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.jupiter)
    testImplementation(libs.truth)

    testImplementation(libs.ktfmt){
//        isTransitive = false
        exclude(group = "org.jetbrains.kotlin")
    }
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    plugins {
        create(property("ID").toString()) {
            id = property("ID").toString()
            implementationClass = property("IMPLEMENTATION_CLASS").toString()
            version = property("VERSION").toString()
            displayName = property("DISPLAY_NAME").toString()
            description = property("DESCRIPTION").toString()
            tags = listOf("ktfmt", "kotlin", "formatter", "reformat", "style", "code", "linter")
        }
    }

    website = property("WEBSITE").toString()
    vcsUrl = property("VCS_URL").toString()
}

signing {
    val signingKey = findProperty("SIGNING_KEY") as? String
    val signingPwd = findProperty("SIGNING_PWD") as? String
    useInMemoryPgpKeys(signingKey, signingPwd)
}

tasks.withType<Test> { useJUnitPlatform() }

val persistKtfmtVersion by
    tasks.registering {
        inputs.property("ktfmtVersion", libs.ktfmt)
        outputs.files(layout.buildDirectory.file("ktfmt-version.txt"))
        doLast { outputs.files.singleFile.writeText(inputs.properties["ktfmtVersion"].toString()) }
    }

tasks.named<ProcessResources>("processResources") {
    from(persistKtfmtVersion) { into("com/ncorti/ktfmt/gradle") }
}
