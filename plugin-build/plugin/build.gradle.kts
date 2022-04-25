import org.gradle.api.internal.classpath.ModuleRegistry
import org.gradle.configurationcache.extensions.serviceOf
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
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
integrationTestRuntime.extendsFrom(configurations.getByName("compileOnly"))

tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(integrationTestRuntime)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(Libs.COROUTINES_CORE)
    implementation(Libs.KTFMT)
    implementation(Libs.DIFF_UTILS)

    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(Libs.AGP)

    testImplementation(TestingLib.COROUTINES_TEST)
    testImplementation(kotlin("gradle-plugin"))
    testImplementation(Libs.AGP)

    testImplementation(platform(TestingLib.JUNIT_BOM))
    testImplementation(TestingLib.JUPITER)
    testImplementation(TestingLib.TRUTH)

    testRuntimeOnly(
        files(
            serviceOf<ModuleRegistry>().getModule("gradle-tooling-api-builders")
                .classpath.asFiles.first()
        )
    )
}

gradlePlugin {
    plugins {
        create(PluginCoordinates.ID) {
            id = PluginCoordinates.ID
            implementationClass = PluginCoordinates.IMPLEMENTATION_CLASS
            version = PluginCoordinates.VERSION
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = PluginBundle.WEBSITE
    vcsUrl = PluginBundle.VCS
    description = PluginBundle.DESCRIPTION
    tags = PluginBundle.TAGS

    plugins {
        getByName(PluginCoordinates.ID) {
            displayName = PluginBundle.DISPLAY_NAME
        }
    }
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
