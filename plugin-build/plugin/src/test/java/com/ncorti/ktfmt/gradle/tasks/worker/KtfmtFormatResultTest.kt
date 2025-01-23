package com.ncorti.ktfmt.gradle.tasks.worker

import com.google.common.truth.Truth.assertThat
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatFailure
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatSkipped
import com.ncorti.ktfmt.gradle.tasks.worker.KtfmtFormatResult.KtfmtFormatSuccess
import java.io.File
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class KtfmtFormatResultTest {

    private val testPairs =
        listOf(
            KtfmtFormatSuccess(File("file1.kt"), true) to "success,true,file1.kt",
            KtfmtFormatSuccess(File("file1.kt"), false) to "success,false,file1.kt",
            KtfmtFormatFailure(File("file2.kt")) to "failure,false,file2.kt",
            KtfmtFormatSkipped(File("file3.kt")) to "skipped,false,file3.kt",
        )

    @TestFactory
    fun testSerialize() =
        testPairs.map { (result, serializedString) ->
            DynamicTest.dynamicTest("Should serialize $result") {
                val actual = result.serialize()

                assertThat(actual).isEqualTo(serializedString)
            }
        }

    @TestFactory
    fun testDeserialize() =
        testPairs.map { (result, serializedString) ->
            DynamicTest.dynamicTest("Should serialize $result") {
                val actual = KtfmtFormatResult.parse(serializedString)

                assertThat(actual).isEqualTo(result)
            }
        }
}
