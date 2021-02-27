package com.ncorti.ktfmt.gradle

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.FormattingOptionsBean.Style.*
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_BLOCK_INDENT
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_CONTINUATION_INDENT
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_DEBUGGING_PRINT_OPTS
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_MAX_WIDTH
import com.ncorti.ktfmt.gradle.KtfmtExtension.Companion.DEFAULT_REMOVE_UNUSED_IMPORTS
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class KtfmtExtensionTest {

    @Test
    fun `maxWidth has default value`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        assertThat(extension.maxWidth.get()).isEqualTo(DEFAULT_MAX_WIDTH)
    }

    @Test
    fun `blockIndent has default value`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        assertThat(extension.blockIndent.get()).isEqualTo(DEFAULT_BLOCK_INDENT)
    }

    @Test
    fun `continuationIndent has default value`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        assertThat(extension.continuationIndent.get()).isEqualTo(DEFAULT_CONTINUATION_INDENT)
    }

    @Test
    fun `removeUnusedImports has default value`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        assertThat(extension.removeUnusedImports.get()).isEqualTo(DEFAULT_REMOVE_UNUSED_IMPORTS)
    }

    @Test
    fun `debuggingPrintOpsAfterFormatting has default value`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        assertThat(extension.debuggingPrintOpsAfterFormatting.get())
            .isEqualTo(DEFAULT_DEBUGGING_PRINT_OPTS)
    }

    @Test
    fun `dropboxStyle configures correctly`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        extension.dropboxStyle()

        assertThat(extension.blockIndent.get()).isEqualTo(4)
        assertThat(extension.continuationIndent.get()).isEqualTo(4)
        assertThat(extension.ktfmtStyle).isEqualTo(DROPBOX)
    }

    @Test
    fun `googleStyle configures correctly`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        extension.googleStyle()

        assertThat(extension.blockIndent.get()).isEqualTo(2)
        assertThat(extension.continuationIndent.get()).isEqualTo(2)
        assertThat(extension.ktfmtStyle).isEqualTo(GOOGLE)
    }

    @Test
    fun `kotlinLangStyle configures correctly`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        extension.kotlinLangStyle()

        assertThat(extension.blockIndent.get()).isEqualTo(4)
        assertThat(extension.continuationIndent.get()).isEqualTo(4)
        assertThat(extension.ktfmtStyle).isEqualTo(GOOGLE)
    }

    @Test
    fun `toBean copies fields correctly`() {
        val extension = object : KtfmtExtension(ProjectBuilder.builder().build()) {}

        extension.maxWidth.set(42)
        extension.blockIndent.set(43)
        extension.continuationIndent.set(44)
        extension.removeUnusedImports.set(false)
        extension.debuggingPrintOpsAfterFormatting.set(true)

        val bean = extension.toBean()

        assertThat(bean.maxWidth).isEqualTo(42)
        assertThat(bean.blockIndent).isEqualTo(43)
        assertThat(bean.continuationIndent).isEqualTo(44)
        assertThat(bean.removeUnusedImports).isEqualTo(false)
        assertThat(bean.debuggingPrintOpsAfterFormatting).isEqualTo(true)
    }
}
