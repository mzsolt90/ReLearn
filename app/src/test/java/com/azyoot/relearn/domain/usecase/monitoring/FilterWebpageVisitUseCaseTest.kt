package com.azyoot.relearn.domain.usecase.monitoring

import com.azyoot.relearn.util.UrlProcessing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.burst.BurstJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentMatchers

@RunWith(BurstJUnit4::class)
class FilterWebpageVisitUseCaseTest {

    private val mockUrlProcessing: UrlProcessing = mock()

    private val usecase = FilterWebpageVisitUseCase(mockUrlProcessing)

    @Test
    fun `Given webpage visit with invalid url test case When isWebpageVisitValid called then webpage visit is deemed invalid`(
        testCase: UrlTestCase
    ) {
        whenever(mockUrlProcessing.isValidUrl(testCase.url)).thenReturn(false)

        val result = usecase.isWebpageVisitValid(testCase.url)

        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `Given webpage visit with valid url test case When isWebpageVisitValid called then webpage visit is properly identified`(
        testCase: UrlTestCase
    ) {
        whenever(mockUrlProcessing.isValidUrl(testCase.url)).thenReturn(true)
        whenever(mockUrlProcessing.ensureStartsWithHttpsScheme(ArgumentMatchers.anyString())).then(
            AdditionalAnswers.returnsFirstArg<String>()
        )

        val result = usecase.isWebpageVisitValid(testCase.url)

        assertThat(result).isEqualTo(testCase.isValid)
    }

    enum class UrlTestCase(val url: String, val isValid: Boolean) {
        NO_PATH("https://wiktionary.org", false),
        NO_PATH_ENDING_SLASH("https://wiktionary.org/", false),
        WRONG_HOST("https://google.com/", false),
        PROPER_ARTICLE("https://wiktionary.org/благополучие", true),
        PROPER_ARTICLE_SUBPATH("https://wiktionary.org/wiki/благополучие", true)
    }
}