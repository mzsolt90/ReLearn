package com.azyoot.relearn.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.entity.WebpageVisit
import com.azyoot.relearn.domain.usecase.parsing.DownloadWebpageAndExtractTranslationUseCase
import com.azyoot.relearn.domain.usecase.parsing.ExtractWiktionaryTranslationUseCase
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime

class DownloadWebpageAndExtractTranslationUseCaseTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockOkHttpClient: OkHttpClient = mock()
    private val mockExtractUseCase: ExtractWiktionaryTranslationUseCase = mock()

    private lateinit var executingOkHttpClient: OkHttpClient
    private lateinit var givenUseCase: DownloadWebpageAndExtractTranslationUseCase

    @Before
    fun setup() {
        executingOkHttpClient = OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(2))
            .readTimeout(Duration.ofSeconds(2))
            .writeTimeout(Duration.ofSeconds(2)).build()
        givenUseCase =
            DownloadWebpageAndExtractTranslationUseCase(
                mockOkHttpClient,
                mockExtractUseCase
            )
        whenever(
            mockExtractUseCase.extractTranslationsFromWiktionaryPage(
                any(),
                any()
            )
        ).then { listOf<WebpageTranslation>() }
    }

    private fun MockWebServer.replaceHost(originalUrl: HttpUrl) =
        url(originalUrl.toUri().path.toString())

    private fun MockWebServer.runOnMockServer(block: MockWebServer.() -> Unit) = run {
        start()
        argumentCaptor<Request> {
            whenever(mockOkHttpClient.newCall(capture())).then {
                executingOkHttpClient.newCall(
                    firstValue.newBuilder().url(replaceHost(firstValue.url)).build()
                )
            }
        }
        block(this)
        shutdown()
    }

    private fun givenMockWebServerAndResult(
        body: String,
        responseCode: Int = 200,
        sendResponse: Boolean = true
    ) =
        MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setBody(body)
                    .setHttp2ErrorCode(responseCode)
                    .setResponseCode(responseCode)
                    .setSocketPolicy(if (sendResponse) SocketPolicy.CONTINUE_ALWAYS else SocketPolicy.NO_RESPONSE)
            )
        }

    private fun givenWebpageVisit(url: String) = WebpageVisit(url, TEST_APP_PACKAGE, TEST_TIME, 0)


    @Test
    fun `Given timeout When download and extract called Then no extraction happens and exception is thrown`() {
        givenMockWebServerAndResult("", sendResponse = false).runOnMockServer {
            val givenVisit = givenWebpageVisit(url(TEST_URL).toString())

            assertThatThrownBy {
                runBlocking {
                    givenUseCase.downloadWebpageAndExtractTranslation(givenVisit)
                }
            }.isInstanceOf(IOException::class.java)

            then(mockExtractUseCase).shouldHaveNoInteractions()
        }
    }

    @Test
    fun `Given error code When download and extract called Then no extraction happens`() {
        givenMockWebServerAndResult("", 500).runOnMockServer {
            val givenVisit = givenWebpageVisit(url(TEST_URL).toString())

            assertThatThrownBy {
                runBlocking {
                    givenUseCase.downloadWebpageAndExtractTranslation(givenVisit)
                }
            }.isInstanceOf(IOException::class.java)

            then(mockExtractUseCase).shouldHaveNoInteractions()
        }
    }

    @Test
    fun `Given wiktionary webpage When download and extract called Then extraction happens`() {
        givenMockWebServerAndResult(TEST_BODY).runOnMockServer {
            val givenVisit = givenWebpageVisit(url(TEST_URL).toString())

            runBlocking {
                givenUseCase.downloadWebpageAndExtractTranslation(givenVisit)
            }

            argumentCaptor<Request> {
                then(mockOkHttpClient).should().newCall(capture())
                assertThat(firstValue.url.toString()).isEqualTo(TEST_URL)
            }
            then(mockExtractUseCase).should()
                .extractTranslationsFromWiktionaryPage(eq(givenVisit), eq(TEST_BODY))
        }
    }

    companion object {
        val TEST_TIME = LocalDateTime.of(2020, 2, 2, 23, 15, 35)
        const val TEST_APP_PACKAGE = "com.google.chrome"
        const val TEST_BODY = "<html></html>"
        const val TEST_URL =
            "https://en.wiktionary.org/wiki/%D0%B7%D0%B0%D0%BF%D0%BE%D0%B2%D0%B5%D0%B4%D0%BD%D0%B8%D0%BA"
    }
}