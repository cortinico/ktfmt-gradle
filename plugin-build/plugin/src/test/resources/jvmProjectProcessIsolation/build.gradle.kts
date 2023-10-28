import com.ncorti.ktfmt.gradle.GradleWorkerIsolationStrategy

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.ncorti.ktfmt.gradle")
}

repositories { mavenCentral() }

ktfmt {
    kotlinLangStyle()
    gradleWorkerIsolationStrategy.set(GradleWorkerIsolationStrategy.NO_ISOLATION)
}
