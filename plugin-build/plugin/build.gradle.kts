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
}

gradlePlugin {
    plugins {
        create(property("ID").toString()) {
            id = property("ID").toString()
            implementationClass = property("IMPLEMENTATION_CLASS").toString()
            version = property("VERSION").toString()
            displayName = property("DISPLAY_NAME").toString()
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = property("WEBSITE").toString()
    vcsUrl = property("VCS_URL").toString()
    description = property("DESCRIPTION").toString()
    tags = listOf("ktfmt", "kotlin", "formatter", "reformat", "style", "code", "linter", "plugin", "gradle")
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
