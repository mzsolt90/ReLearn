package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.domain.usecase.parsing.ExtractWiktionaryTranslationUseCase
import okhttp3.OkHttpClient
import okhttp3.Request
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtractWiktionaryTranslationUseCaseTest {

    data class TranslationTestCase(
        val title: String,
        val toTextList: List<String>,
        val url: String
    )

    private val usecase =
        ExtractWiktionaryTranslationUseCase()

    private val okHttpClient = OkHttpClient.Builder().build()

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
                downloadUrl(testCase.url) ?: ""
            )

            val expectedTranslations = testCase.toTextList.map { toText ->
                WebpageTranslation(testCase.title, toText, givenWebpageVisit)
            }

            assertThat(extractedTranslations).isEqualTo(expectedTranslations)
        }
    }

    fun downloadUrl(url: String) =
        okHttpClient.newCall(Request.Builder().url(url).get().build()).execute().body?.string()

    companion object {
        const val TEST_URL =
            "https://en.wiktionary.org/wiki/%D0%B7%D0%B0%D0%BF%D0%BE%D0%B2%D0%B5%D0%B4%D0%BD%D0%B8%D0%BA"

        val TEST_TRANSLATIONS = listOf(
            TranslationTestCase(
                "запове́дник",
                listOf("natural reserve"),
                "https://en.wiktionary.org/wiki/заповедник"
            ),
            TranslationTestCase(
                "утоми́ть",
                listOf("to fatigue, to weary, to tire"),
                "https://en.wiktionary.org/wiki/утомить"
            ),
            TranslationTestCase(
                "утомлённый",
                listOf(
                    "tired (experiencing fatigue; of a person, nerves, etc.)",
                    "tired (expressing fatigue; of a look, face, tone, etc.; inanimate)"
                ),
                "https://en.wiktionary.org/wiki/утомлённый"
            ),
            TranslationTestCase(
                "до́брый",
                listOf(
                    "kind, good, genial",
                    "kindly, good-hearted",
                    "gracious, nice",
                    "gentle, soft",
                    "decent",
                    "benign",
                    "beneficent",
                    "tenderhearted",
                    "good-natured",
                    "kindhearted, warm-hearted",
                    "(colloquial) good, solid ― идти́ до́брых де́сять киломе́тров ― to walk a good/solid ten kilometres"
                ),
                "https://en.wiktionary.org/wiki/добрый"
            ),
            TranslationTestCase(
                "дом", listOf(
                    "house (building) ― обы́скивать дом за до́мом ― to search house by house",
                    "building ― Synonym: зда́ние (zdánije)",
                    "home",
                    "family",
                    "household"
                ),
                "https://en.wiktionary.org/wiki/дом"
            ),
            TranslationTestCase(
                "перераспределе́ниях", listOf(),
                "https://en.wiktionary.org/wiki/перераспределениях"
            )
        )
    }
}
