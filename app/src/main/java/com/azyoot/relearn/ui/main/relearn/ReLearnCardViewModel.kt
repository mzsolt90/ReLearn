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
            viewState.value = ReLearnCardViewState.FinishedLoading(translation)
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
            viewState.value = ReLearnCardViewState.FinishedLoading(translation)
        }
    }

    fun acceptReLearn() {
        val relearn =
            currentViewState.let { if (it is ReLearnCardViewState.FinishedLoading) it.reLearnTranslation else return }
        coroutineScope.launch {
            viewState.value = ReLearnCardViewState.Accepting(relearn)
            val newSource = acceptRelearnSourceUseCase.acceptRelearnUseCase(relearn.source)
            val translation = getTranslationFromSourceUseCase.getTranslationFromSource(newSource)
            viewState.value = ReLearnCardViewState.Accepted(translation)
        }
    }

    fun deleteReLearn() {
        val relearn =
            currentViewState.let { if (it is ReLearnCardViewState.ReLearnTranslationState) it.reLearnTranslation else return }
        coroutineScope.launch {
            //this viewmodel can now be reused
            setReLearnDeletedUseCase.setReLearnDeleted(relearn.source, true)
            viewState.value = ReLearnCardViewState.Deleted(relearn)
        }
    }

    fun undeleteReLearn(reLearnTranslation: ReLearnTranslation) {
        viewState.value = ReLearnCardViewState.FinishedLoading(reLearnTranslation)
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