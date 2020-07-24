package com.azyoot.relearn.ui.main.cards

import com.azyoot.relearn.domain.usecase.relearn.*
import com.azyoot.relearn.ui.common.viewmodels.BaseAndroidViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ReLearnCardViewModel
@Inject constructor(
    private val getNextAndShowReLearnUseCase: GetNextAndShowReLearnUseCase,
    private val getNthHistoryReLearnSourceUseCase: GetNthHistoryReLearnSourceUseCase,
    private val getTranslationFromSourceUseCase: GetTranslationFromSourceUseCase,
    private val acceptRelearnSourceUseCase: AcceptRelearnSourceUseCase,
    private val setReLearnDeletedUseCase: SetReLearnDeletedUseCase
) : BaseAndroidViewModel<ReLearnCardViewState, ReLearnCardEffect>(ReLearnCardViewState.Initial) {

    fun loadInitialNthHistory(n: Int) {
        if (currentViewState !is ReLearnCardViewState.Initial) return
        coroutineScope.launch {
            viewState.value = ReLearnCardViewState.Loading
            val source = getNthHistoryReLearnSourceUseCase.getNthHistoryReLearnSourceUseCase(n)
            if (source == null) {
                viewState.value = ReLearnCardViewState.NotFound
                return@launch
            }
            val translation = getTranslationFromSourceUseCase.getTranslationFromSource(source)
            viewState.value = ReLearnCardViewState.ReLearnTranslationState(
                translation,
                false,
                ReLearnCardReLearnState.FinishedLoading
            )
        }
    }

    fun loadInitialNextReLearn() {
        if (currentViewState !is ReLearnCardViewState.Initial) return
        coroutineScope.launch {
            viewState.value = ReLearnCardViewState.Loading
            val source = getNextAndShowReLearnUseCase.getNextAndShowReLearnUseCase()
            if (source == null) {
                viewState.value = ReLearnCardViewState.NotFound
                return@launch
            }
            val translation = getTranslationFromSourceUseCase.getTranslationFromSource(source)
            viewState.value = ReLearnCardViewState.ReLearnTranslationState(
                translation,
                false,
                ReLearnCardReLearnState.FinishedLoading
            )
        }
    }

    fun acceptReLearn() {
        val relearnState =
            currentViewState as? ReLearnCardViewState.ReLearnTranslationState ?: return
        coroutineScope.launch {
            viewState.value = relearnState.copy(relearnState = ReLearnCardReLearnState.Accepting)
            val newSource =
                acceptRelearnSourceUseCase.acceptRelearnUseCase(relearnState.reLearnTranslation.source)
            val translation = getTranslationFromSourceUseCase.getTranslationFromSource(newSource)
            viewState.value = relearnState.copy(
                reLearnTranslation = translation,
                relearnState = ReLearnCardReLearnState.Accepted
            )
        }
    }

    fun deleteReLearn() {
        val relearnState =
            currentViewState as? ReLearnCardViewState.ReLearnTranslationState ?: return
        val relearn = relearnState.reLearnTranslation
        coroutineScope.launch {
            //this viewmodel can now be reused
            setReLearnDeletedUseCase.setReLearnDeleted(relearn.source, true)
            sendEffect(ReLearnCardEffect.ReLearnDeleted(relearnState))
            viewState.value = relearnState.copy(relearnState = ReLearnCardReLearnState.Deleted)
        }
    }

    fun undeleteReLearn(state: ReLearnCardViewState.ReLearnTranslationState) {
        viewState.value = state
        coroutineScope.launch {
            setReLearnDeletedUseCase.setReLearnDeleted(state.reLearnTranslation.source, false)
        }
    }

    fun launchReLearn() {
        sendEffect(
            ReLearnCardEffect.Launch(
                (currentViewState as? ReLearnCardViewState.ReLearnTranslationState)?.reLearnTranslation
                    ?: return
            )
        )
        setExpanded(true)
    }

    fun setExpanded(isExpanded: Boolean) {
        val relearnState =
            currentViewState as? ReLearnCardViewState.ReLearnTranslationState ?: return
        viewState.value = relearnState.copy(isRevealed = isExpanded)
    }
}