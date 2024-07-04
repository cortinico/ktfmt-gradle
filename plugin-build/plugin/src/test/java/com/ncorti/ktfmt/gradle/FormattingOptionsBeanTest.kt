package com.ncorti.ktfmt.gradle

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.tasks.worker.toFormattingOptions
import org.junit.jupiter.api.Test

class FormattingOptionsBeanTest {

    @Test
    fun `toFormattingOptions copies fields correctly`() {
        val bean =
            FormattingOptionsBean(
                maxWidth = 42,
                blockIndent = 43,
                continuationIndent = 44,
                removeUnusedImports = false,
                manageTrailingCommas = true,
                debuggingPrintOpsAfterFormatting = true,
            )

        val opts = bean.toFormattingOptions()

        assertThat(opts.maxWidth).isEqualTo(42)
        assertThat(opts.blockIndent).isEqualTo(43)
        assertThat(opts.continuationIndent).isEqualTo(44)
        assertThat(opts.removeUnusedImports).isEqualTo(false)
        assertThat(opts.manageTrailingCommas).isEqualTo(true)
        assertThat(opts.debuggingPrintOpsAfterFormatting).isEqualTo(true)
    }
}
