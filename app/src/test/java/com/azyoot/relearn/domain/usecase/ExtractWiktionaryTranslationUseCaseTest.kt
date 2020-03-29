package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.entity.WebpageVisit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.BufferedReader

class ExtractWiktionaryTranslationUseCaseTest {

    data class TranslationTestCase(
        val fromText: String,
        val toTextList: List<String>,
        val body: String
    )

    private val usecase = ExtractWiktionaryTranslationUseCase()

    private val givenWebpageVisit = WebpageVisit(
        TEST_URL,
        DownloadWebpageAndExtractTranslationUseCaseTest.TEST_APP_PACKAGE,
        DownloadWebpageAndExtractTranslationUseCaseTest.TEST_TIME,
        0
    )

    @Test
    fun `Given empty webpage visit When extract called Then nothing is extracted`() {
        val extractedTranslation =
            usecase.extractTranslationsFromWiktionaryPage(givenWebpageVisit, "")

        assertThat(extractedTranslation).isEmpty()
    }

    @Test
    fun `Given webpage visits with test translations When extract called Then translation extracted properly`() {
        TEST_TRANSLATIONS.forEach { testCase ->
            val extractedTranslations = usecase.extractTranslationsFromWiktionaryPage(
                givenWebpageVisit,
                javaClass.classLoader!!.getResourceAsStream(testCase.body).bufferedReader()
                    .use(BufferedReader::readText)
            )

            val expectedTranslations = testCase.toTextList.map { toText ->
                WebpageTranslation(testCase.fromText, toText, givenWebpageVisit)
            }

            assertThat(extractedTranslations).isEqualTo(expectedTranslations)
        }
    }

    companion object {
        const val TEST_URL =
            "https://en.wiktionary.org/wiki/%D0%B7%D0%B0%D0%BF%D0%BE%D0%B2%D0%B5%D0%B4%D0%BD%D0%B8%D0%BA"

        val TEST_TRANSLATIONS = listOf(
            TranslationTestCase("запове́дник", listOf("natural reserve"), "заповедник.html"),
            TranslationTestCase(
                "утомлённый",
                listOf(
                    "tired (experiencing fatigue; of a person, nerves, etc.)",
                    "tired (expressing fatigue; of a look, face, tone, etc.; inanimate)"
                ),
                "утомлённый.html"
            )
        )
    }
}
