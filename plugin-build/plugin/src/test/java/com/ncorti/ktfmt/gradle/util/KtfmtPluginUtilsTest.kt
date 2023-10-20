package com.ncorti.ktfmt.gradle.util

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.KtfmtPluginUtils.shouldCreateTasks
import org.junit.jupiter.api.Test

class KtfmtPluginUtilsTest {

    @Test
    fun `shouldCreateTasks returns true for main sourceset`() {
        assertThat(shouldCreateTasks("main")).isTrue()
    }

    @Test
    fun `shouldCreateTasks returns false for KSP sourceset`() {
        assertThat(shouldCreateTasks("generatedByKspKotlin")).isFalse()
    }

    @Test
    fun `shouldCreateTasks returns false for Spring sourceset`() {
        assertThat(shouldCreateTasks("aot")).isFalse()
        assertThat(shouldCreateTasks("aotTest")).isFalse()
    }
}
