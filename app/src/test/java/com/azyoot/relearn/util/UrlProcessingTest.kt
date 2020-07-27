package com.azyoot.relearn.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URLEncoder

@RunWith(RobolectricTestRunner::class)
class UrlProcessingTest {

    private val urlProcessing = UrlProcessing()

    @Test
    fun `Given url with fragments When stripFragmentFromUrl called Then no more fragments in result`() {
        val url = "http://wiktionary.org/бог#Russian"

        val result = urlProcessing.stripFragmentFromUrl(url)

        assertThat(result).isEqualTo("http://wiktionary.org/бог")
    }

    @Test
    fun `Given url with no fragments When stripFragmentFromUrl called Then url is unchanged`() {
        val url = "http://wiktionary.org/бог"

        val result = urlProcessing.stripFragmentFromUrl(url)

        assertThat(result).isEqualTo(url)
    }

    @Test
    fun `Given url with no scheme When ensureStartsWithHttpsScheme called Then https is prepended`() {
        val url = "wiktionary.org/бог"

        val result = urlProcessing.ensureStartsWithHttpsScheme(url)

        assertThat(result).isEqualTo("https://wiktionary.org/бог")
    }

    @Test
    fun `Given url with http When ensureStartsWithHttpsScheme called Then http is replaced to https`() {
        val url = "http://wiktionary.org/бог"

        val result = urlProcessing.ensureStartsWithHttpsScheme(url)

        assertThat(result).isEqualTo("https://wiktionary.org/бог")
    }

    @Test
    fun `Given url with https When ensureStartsWithHttpsScheme called Then url is unchanged`() {
        val url = "https://wiktionary.org/бог"

        val result = urlProcessing.ensureStartsWithHttpsScheme(url)

        assertThat(result).isEqualTo(url)
    }

    @Test
    fun `Given invalid url When isValidUrl called Then url is not valid`() {
        InvalidUrl.values().forEach {
            val result = urlProcessing.isValidUrl(it.url)

            assertThat(result).isEqualTo(false)
        }
    }

    @Test
    fun `Given valid url When isValidUrl called Then url valid`() {
        val result = urlProcessing.isValidUrl("https://google.com")

        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `Given url encoded url When decoded Then original is returned`() {
        val url = URLEncoder.encode("http://wiktionary.org/бог", "UTF-8")

        val decoded = urlProcessing.urlDecode(url)

        assertThat(decoded).isEqualTo("http://wiktionary.org/бог")
    }

    @Test
    fun `Given partially url encoded url When decoded Then only encoded part is decoded`() {
        val url =
            "https://en.m.wiktionary.org/wiki/%D0%B4%D0%B2%D0%B8%D0%BD%D1%83%D1%82%D1%8C#Russian"

        val decoded = urlProcessing.urlDecode(url)

        assertThat(decoded).isEqualTo("https://en.m.wiktionary.org/wiki/двинуть#Russian")
    }

    enum class InvalidUrl(val url: String) {
        INVALID_SCHEME("://wiktionary.org/бог"),
        NO_HOST("https://Search"),
        SHORT_TLD("https://wiktionary.o")
    }
}