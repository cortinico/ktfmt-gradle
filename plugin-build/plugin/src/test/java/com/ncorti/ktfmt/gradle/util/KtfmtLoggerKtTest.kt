package com.ncorti.ktfmt.gradle.util

import com.google.common.truth.Truth.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KtfmtLoggerKtTest {

    private val logger = TestLogger(ProjectBuilder.builder().build())

    @BeforeEach
    fun setUp() {
        logger.reset()
    }

    @Test
    fun `i prints to info`() {
        logger.i("Logging started...")

        assertThat(logger.warnMessages).isEmpty()
        assertThat(logger.errorMessages).isEmpty()
        assertThat(logger.infoMessages).hasSize(1)
        assertThat(logger.infoMessages.first()).isEqualTo("[ktfmt] Logging started...")
    }

    @Test
    fun `w prints to warn`() {
        logger.w("Something happened")

        assertThat(logger.infoMessages).isEmpty()
        assertThat(logger.errorMessages).isEmpty()
        assertThat(logger.warnMessages).hasSize(1)
        assertThat(logger.warnMessages.first()).isEqualTo("[ktfmt] Something happened")
    }

    @Test
    fun `e prints to warn`() {
        logger.e("Fatal error!")

        assertThat(logger.infoMessages).isEmpty()
        assertThat(logger.warnMessages).isEmpty()
        assertThat(logger.errorMessages).hasSize(1)
        assertThat(logger.errorMessages.first()).isEqualTo("[ktfmt] Fatal error!")
    }
}
