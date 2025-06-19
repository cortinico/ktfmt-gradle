package com.ncorti.ktfmt.gradle

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_BLOCK_INDENT
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_CONTINUATION_INDENT
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_DEBUGGING_PRINT_OPTS
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_MAX_WIDTH
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_REMOVE_UNUSED_IMPORTS
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_USE_CLASSLOADER_ISOLATION
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class KtfmtExtensionTest {

    @Test
    fun `maxWidth has default value`() {
        val extension = createExtension()

        assertThat(extension.maxWidth.get()).isEqualTo(DEFAULT_MAX_WIDTH)
    }

    @Test
    fun `blockIndent has default value`() {
        val extension = createExtension()

        assertThat(extension.blockIndent.get()).isEqualTo(DEFAULT_BLOCK_INDENT)
    }

    @Test
    fun `continuationIndent has default value`() {
        val extension = createExtension()

        assertThat(extension.continuationIndent.get()).isEqualTo(DEFAULT_CONTINUATION_INDENT)
    }

    @Test
    fun `removeUnusedImports has default value`() {
        val extension = createExtension()

        assertThat(extension.removeUnusedImports.get()).isEqualTo(DEFAULT_REMOVE_UNUSED_IMPORTS)
    }

    @Test
    fun `debuggingPrintOpsAfterFormatting has default value`() {
        val extension = createExtension()

        assertThat(extension.debuggingPrintOpsAfterFormatting.get())
            .isEqualTo(DEFAULT_DEBUGGING_PRINT_OPTS)
    }

    @Test
    fun `useClassloaderIsolation has default value`() {
        val extension = createExtension()

        assertThat(extension.useClassloaderIsolation.get())
            .isEqualTo(DEFAULT_USE_CLASSLOADER_ISOLATION)
    }

    @Test
    fun `googleStyle configures correctly`() {
        val extension = createExtension()

        extension.googleStyle()

        assertThat(extension.blockIndent.get()).isEqualTo(2)
        assertThat(extension.continuationIndent.get()).isEqualTo(2)
    }

    @Test
    fun `kotlinLangStyle configures correctly`() {
        val extension = createExtension()

        extension.kotlinLangStyle()

        assertThat(extension.blockIndent.get()).isEqualTo(4)
        assertThat(extension.continuationIndent.get()).isEqualTo(4)
    }

    @Test
    fun `toFormattingOptions copies fields correctly`() {
        val extension = createExtension()

        extension.maxWidth.set(42)
        extension.blockIndent.set(43)
        extension.continuationIndent.set(44)
        extension.removeUnusedImports.set(false)
        extension.manageTrailingCommas.set(true)
        extension.debuggingPrintOpsAfterFormatting.set(true)

        val bean = extension.toFormattingOptions()

        assertThat(bean.maxWidth).isEqualTo(42)
        assertThat(bean.blockIndent).isEqualTo(43)
        assertThat(bean.continuationIndent).isEqualTo(44)
        assertThat(bean.removeUnusedImports).isEqualTo(false)
        assertThat(bean.manageTrailingCommas).isEqualTo(true)
        assertThat(bean.debuggingPrintOpsAfterFormatting).isEqualTo(true)
    }

    private fun createExtension(): KtfmtExtension {
        val project = ProjectBuilder.builder().build()
        return project.extensions.create("ktfmtExt", KtfmtExtension::class.java)
    }
}
