plugins {
    `kotlin-dsl`
    id("com.ncorti.ktfmt.gradle") version "0.4.0"
}

repositories {
    jcenter()
}

ktfmt {
    kotlinLangStyle()
}