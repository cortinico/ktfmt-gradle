import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.configurationcache.extensions.serviceOf
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    alias(libs.plugins.pluginPublish)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

/**
 * We create a configuration that we can resolve by extending [compileOnly].
 * Here we inject the dependencies into TestKit plugin
 * classpath via [PluginUnderTestMetadata] to make them available for testing.
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

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        jvmTarget = JavaVersion.VERSION_11.toString()
        apiVersion = "1.4"
        languageVersion = "1.4"
    }
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.ktfmt)
    implementation(libs.diffUtils)

    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(libs.agp)
    
    testImplementation(libs.coroutines.test)
    testImplementation(kotlin("gradle-plugin"))
    testImplementation(libs.agp)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.jupiter)
    testImplementation(libs.truth)

    testRuntimeOnly(
        files(
            serviceOf<ModuleRegistry>().getModule("gradle-tooling-api-builders")
                .classpath.asFiles.first()
        )
    )
    constraints {
        implementation(libs.kotlin.compiler.embeddable) {
            because("Clash in Kotlin compiler versions - See https://youtrack.jetbrains.com/issue/KT-54236")
        }
    }
}

gradlePlugin {
    plugins {
        create(property("ID").toString()) {
            id = property("ID").toString()
            implementationClass = property("IMPLEMENTATION_CLASS").toString()
            version = property("VERSION").toString()
            displayName = property("DISPLAY_NAME").toString()
            description = property("DESCRIPTION").toString()
            tags.set(listOf("ktfmt", "kotlin", "formatter", "reformat", "style", "code", "linter"))
        }
    }
}

gradlePlugin {
    website.set(property("WEBSITE").toString())
    vcsUrl.set(property("VCS_URL").toString())
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.create("setupPluginUploadFromEnvironment") {
    doLast {
        val key = System.getenv("GRADLE_PUBLISH_KEY")
        val secret = System.getenv("GRADLE_PUBLISH_SECRET")

        if (key == null || secret == null) {
            throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
        }

        System.setProperty("gradle.publish.key", key)
        System.setProperty("gradle.publish.secret", secret)
    }
}
