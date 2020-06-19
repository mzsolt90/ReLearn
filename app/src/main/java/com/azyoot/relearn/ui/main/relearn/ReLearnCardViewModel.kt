package com.azyoot.relearn.ui.main.relearn

import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.domain.usecase.relearn.*
import com.azyoot.relearn.ui.common.BaseAndroidViewModel
import kotlinx.coroutines.CoroutineScope
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
    private val setReLearnDeletedUseCase: SetReLearnDeletedUseCase,
    override val coroutineScope: CoroutineScope
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
            viewState.value = relearnState.copy(reLearnTranslation = translation, relearnState = ReLearnCardReLearnState.Accepted)
        }
    }

    fun deleteReLearn() {
        val relearnState =
            currentViewState as? ReLearnCardViewState.ReLearnTranslationState ?: return
        coroutineScope.launch {
            //this viewmodel can now be reused
            setReLearnDeletedUseCase.setReLearnDeleted(relearnState.reLearnTranslation.source, true)
            viewState.value = relearnState.copy(relearnState = ReLearnCardReLearnState.Deleted)
        }
    }

    fun undeleteReLearn(reLearnTranslation: ReLearnTranslation) {
        viewState.value = ReLearnCardViewState.ReLearnTranslationState(
            reLearnTranslation,
            false,
            ReLearnCardReLearnState.FinishedLoading
        )
        coroutineScope.launch {
            setReLearnDeletedUseCase.setReLearnDeleted(reLearnTranslation.source, false)
        }
    }

    fun launchReLearn() {
        sendEffect(
            ReLearnCardEffect.Launch(
                (currentViewState as? ReLearnCardViewState.ReLearnTranslationState)?.reLearnTranslation
                    ?: return
            )
        )
    }
}