plugins {
    kotlin("jvm") version "1.7.20"
    id("com.ncorti.ktfmt.gradle")
}

ktfmt { kotlinLangStyle() }

repositories { mavenCentral() }
