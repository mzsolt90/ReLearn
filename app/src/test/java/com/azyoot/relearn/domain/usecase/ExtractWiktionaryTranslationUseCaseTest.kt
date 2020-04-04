package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.entity.WebpageVisit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.BufferedReader

class ExtractWiktionaryTranslationUseCaseTest {

    data class TranslationTestCase(
        val title: String,
        val toTextList: List<String>,
        val bodyFileName: String
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
                javaClass.classLoader!!.getResourceAsStream(testCase.bodyFileName).bufferedReader()
                    .use(BufferedReader::readText)
            )

            val expectedTranslations = testCase.toTextList.map { toText ->
                WebpageTranslation(testCase.title, toText, givenWebpageVisit)
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
                "утоми́ть",
                listOf("to fatigue, to weary, to tire"),
                "утомить.html"
            ),
            TranslationTestCase(
                "утомлённый",
                listOf(
                    "tired (experiencing fatigue; of a person, nerves, etc.)",
                    "tired (expressing fatigue; of a look, face, tone, etc.; inanimate)"
                ),
                "утомлённый.html"
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
                "добрый.html"
            ),
            TranslationTestCase(
                "дом", listOf(
                    "house (building) ― обы́скивать дом за до́мом ― to search house by house",
                    "home",
                    "family",
                    "household"
                ),
                "дом.html"
            ),
            TranslationTestCase(
                "перераспределе́ниях", listOf(),
                "перераспределениях.html"
            )
        )
    }
}
