package com.azyoot.relearn.ui.main.cards

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.azyoot.relearn.domain.entity.*
import com.azyoot.relearn.domain.usecase.relearn.*
import com.azyoot.relearn.testing.viewmodels.getObservedEffects
import com.azyoot.relearn.testing.viewmodels.getObservedStates
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class ReLearnCardViewModelTest {

    private val mockGetNextAndShowReLearnUseCase: GetNextAndShowReLearnUseCase = mock()
    private val mockGetNthHistoryReLearnSourceUseCase: GetNthHistoryReLearnSourceUseCase = mock()
    private val mockGetTranslationFromSourceUseCase: GetTranslationFromSourceUseCase = mock()
    private val mockAcceptRelearnSourceUseCase: AcceptRelearnSourceUseCase = mock()
    private val mockSetReLearnDeletedUseCase: SetReLearnDeletedUseCase = mock()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testCoroutineScope = TestCoroutineScope()

    @Before
    fun setup() {
        mockGetNextAndShowReLearnUseCase.stub {
            onBlocking { getNextAndShowReLearnUseCase() } doReturn (RELEARN_1.source)
        }
        mockGetNthHistoryReLearnSourceUseCase.stub {
            onBlocking { getNthHistoryReLearnSourceUseCase(ArgumentMatchers.anyInt()) } doReturn (RELEARN_2.source)
        }
        argumentCaptor<ReLearnSource> {
            mockGetTranslationFromSourceUseCase.stub {
                onBlocking { getTranslationFromSource(capture()) } doAnswer {
                    ReLearnTranslation(
                        source = firstValue,
                        sourceText = firstValue.sourceText,
                        translations = if (firstValue == RELEARN_1.source) TRANSLATIONS_1 else TRANSLATIONS_2
                    )
                }
            }
        }
        argumentCaptor<ReLearnSource> {
            mockAcceptRelearnSourceUseCase.stub {
                onBlocking { acceptRelearnUseCase(capture()) } doAnswer { firstValue }
            }
        }
        mockSetReLearnDeletedUseCase.stub {
            onBlocking { setReLearnDeleted(any(), ArgumentMatchers.anyBoolean()) } doReturn (Unit)
        }
    }

    @Test
    fun `Given ViewModel When initialized Then initial state is set`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            val statesObserved = getObservedStates(viewModel)

            assertThat(statesObserved).isEqualTo(listOf(ReLearnCardViewState.Initial))
        }

    private fun ListAssert<ReLearnCardViewState>.hasCardViewStates(
        expected: List<ReLearnCardViewState>,
        startIndex: Int = 0
    ) =
        hasSizeGreaterThanOrEqualTo(expected.size + startIndex).apply {
            expected.forEachIndexed { index, viewState ->
                element(startIndex + index).isEqualTo(viewState)
            }
        }

    private fun ListAssert<ReLearnCardViewState>.hasRevealedStates(
        expected: List<Boolean>,
        startIndex: Int = 0
    ) =
        hasSizeGreaterThanOrEqualTo(expected.size + startIndex).apply {
            expected.forEachIndexed { index, isRevealed ->
                element(startIndex + index)
                    .isOfAnyClassIn(ReLearnCardViewState.ReLearnTranslationState::class.java)
                    .extracting { isRevealed }.isEqualTo(isRevealed)
            }
        }

    private fun ListAssert<ReLearnCardViewState>.hasReLearnViewStates(
        relearn: ReLearnTranslation,
        expected: List<ReLearnCardReLearnState>,
        startIndex: Int = 0
    ) =
        hasSizeGreaterThanOrEqualTo(expected.size + startIndex).apply {
            expected.forEachIndexed { index, viewState ->
                element(startIndex + index)
                    .isOfAnyClassIn(ReLearnCardViewState.ReLearnTranslationState::class.java)
                    .extracting { relearn }.isEqualTo(relearn)
                    .extracting { viewState }.isEqualTo(viewState)
            }
        }

    @Test
    fun `Given ViewModel When loadInitialNextReLearn Then relearn is loaded`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            val statesObserved = getObservedStates(viewModel)

            viewModel.loadInitialNextReLearn()

            verify(mockGetNextAndShowReLearnUseCase).getNextAndShowReLearnUseCase()
            assertThat(statesObserved)
                .hasCardViewStates(
                    listOf(
                        ReLearnCardViewState.Initial,
                        ReLearnCardViewState.Loading
                    )
                )
                .hasReLearnViewStates(
                    RELEARN_1,
                    listOf(ReLearnCardReLearnState.FinishedLoading),
                    2
                )
        }

    @Test
    fun `Given ViewModel When loadInitialNthHistory Then relearn is loaded`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            val statesObserved = getObservedStates(viewModel)

            viewModel.loadInitialNthHistory(1)

            verify(mockGetNthHistoryReLearnSourceUseCase).getNthHistoryReLearnSourceUseCase(1)
            assertThat(statesObserved)
                .hasCardViewStates(
                    listOf(
                        ReLearnCardViewState.Initial,
                        ReLearnCardViewState.Loading
                    )
                )
                .hasReLearnViewStates(
                    RELEARN_2,
                    listOf(ReLearnCardReLearnState.FinishedLoading),
                    2
                )
        }

    @Test
    fun `Given ViewModel When accepting relearn Then relearn is accepted and state is updated`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            viewModel.loadInitialNextReLearn()

            val statesObserved = getObservedStates(viewModel)

            viewModel.acceptReLearn()

            verify(mockAcceptRelearnSourceUseCase).acceptRelearnUseCase(RELEARN_1.source)
            assertThat(statesObserved)
                .hasReLearnViewStates(
                    RELEARN_1,
                    listOf(
                        ReLearnCardReLearnState.FinishedLoading,
                        ReLearnCardReLearnState.Accepting,
                        ReLearnCardReLearnState.Accepted
                    )
                )
        }

    @Test
    fun `Given ViewModel When deleting relearn Then relearn is deleted and state is updated`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            viewModel.loadInitialNextReLearn()

            val statesObserved = getObservedStates(viewModel)

            viewModel.deleteReLearn()

            verify(mockSetReLearnDeletedUseCase).setReLearnDeleted(RELEARN_1.source, true)
            assertThat(statesObserved)
                .hasReLearnViewStates(
                    RELEARN_1,
                    listOf(
                        ReLearnCardReLearnState.FinishedLoading,
                        ReLearnCardReLearnState.Deleted
                    )
                )
        }

    @Test
    fun `Given ViewModel When undeleting relearn Then relearn is undeleted and state is updated`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            viewModel.loadInitialNextReLearn()
            viewModel.deleteReLearn()

            val statesObservedBeforeUndelete = getObservedStates(viewModel)
            val lastState = statesObservedBeforeUndelete.last()

            val statesObservedAfterUndelete = getObservedStates(viewModel)

            viewModel.undeleteReLearn(lastState as ReLearnCardViewState.ReLearnTranslationState)

            verify(mockSetReLearnDeletedUseCase).setReLearnDeleted(RELEARN_1.source, false)
            assertThat(statesObservedAfterUndelete)
                .hasCardViewStates(listOf(lastState))
        }

    @Test
    fun `Given ViewModel When launched Then effect is produced`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            viewModel.loadInitialNthHistory(1)

            val effectsObserved = getObservedEffects(viewModel)
            val statesObserved = getObservedStates(viewModel)

            viewModel.launchReLearn()

            assertThat(effectsObserved).isEqualTo(listOf(ReLearnCardEffect.Launch(RELEARN_2)))
            assertThat(statesObserved).isEqualTo(
                listOf(
                    ReLearnCardViewState.ReLearnTranslationState(
                        RELEARN_2,
                        false,
                        ReLearnCardReLearnState.FinishedLoading
                    ),
                    ReLearnCardViewState.ReLearnTranslationState(
                        RELEARN_2,
                        true,
                        ReLearnCardReLearnState.FinishedLoading
                    )
                )
            )
        }

    @Test
    fun `Given ViewModel When expanded and collapsed Then states are updated`() =
        testCoroutineScope.runBlockingTest {
            val viewModel = givenViewModel()

            viewModel.loadInitialNextReLearn()

            val statesObserved = getObservedStates(viewModel)

            viewModel.setExpanded(true)
            viewModel.setExpanded(false)

            assertThat(statesObserved)
                .hasReLearnViewStates(
                    RELEARN_1,
                    listOf(
                        ReLearnCardReLearnState.FinishedLoading,
                        ReLearnCardReLearnState.FinishedLoading,
                        ReLearnCardReLearnState.FinishedLoading
                    )
                )
                .hasRevealedStates(listOf(true, false), 1)
        }

    private fun givenViewModel() = ReLearnCardViewModel(
        mockGetNextAndShowReLearnUseCase,
        mockGetNthHistoryReLearnSourceUseCase,
        mockGetTranslationFromSourceUseCase,
        mockAcceptRelearnSourceUseCase,
        mockSetReLearnDeletedUseCase,
        testCoroutineScope
    )

    companion object {
        val TRANSLATIONS_1 = listOf(
            "TRANSLATION_1",
            "TRANSLATION_2"
        )
        val TRANSLATIONS_2 = listOf(
            "TRANSLATION_3",
            "TRANSLATION_4"
        )

        val RELEARN_1 = ReLearnTranslation(
            ReLearnSource(
                sourceText = "TITLE",
                latestSourceTime = LocalDateTime.of(2020, 1, 16, 18, 32),
                latestSourceId = 456814,
                latestReLearnTime = LocalDateTime.of(2020, 1, 16, 18, 32),
                latestRelearnStatus = RelearnEventStatus.SHOWING,
                sourceType = SourceType.WEBPAGE_VISIT,
                webpageVisit = WebpageVisit(
                    url = "https://index.hu/",
                    appPackageName = "com.google.chrome",
                    time = LocalDateTime.of(2020, 1, 16, 18, 32),
                    databaseId = 879418,
                    lastParseVersion = 19
                ),
                translationEvent = null
            ),
            sourceText = "TITLE",
            translations = TRANSLATIONS_1
        )
        val RELEARN_2 = ReLearnTranslation(
            ReLearnSource(
                sourceText = "TITLE2",
                latestSourceTime = LocalDateTime.of(2020, 2, 4, 5, 18, 32),
                latestSourceId = 453214,
                latestReLearnTime = LocalDateTime.of(2020, 5, 16, 18, 32),
                latestRelearnStatus = RelearnEventStatus.SHOWING,
                sourceType = SourceType.WEBPAGE_VISIT,
                webpageVisit = WebpageVisit(
                    url = "https://444.hu/",
                    appPackageName = "com.google.chrome",
                    time = LocalDateTime.of(2020, 5, 16, 18, 32),
                    databaseId = 879444,
                    lastParseVersion = 19
                ),
                translationEvent = null
            ),
            sourceText = "TITLE2",
            translations = TRANSLATIONS_2
        )
    }
}