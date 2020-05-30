package com.azyoot.relearn.ui.main.relearn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.domain.usecase.relearn.*
import com.azyoot.relearn.ui.main.MainViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReLearnCardViewModel @Inject constructor(
    private val getNextAndShowReLearnUseCase: GetNextAndShowReLearnUseCase,
    private val getNthHistoryReLearnSourceUseCase: GetNthHistoryReLearnSourceUseCase,
    private val getTranslationFromSourceUseCase: GetTranslationFromSourceUseCase,
    private val acceptRelearnSourceUseCase: AcceptRelearnSourceUseCase,
    private val setReLearnDeletedUseCase: SetReLearnDeletedUseCase,
    private val coroutineScope: CoroutineScope
) {

    private val stateInternal = MutableLiveData<ReLearnCardViewState>()
    val stateLiveData: LiveData<ReLearnCardViewState>
        get() = stateInternal

    val currentState: ReLearnCardViewState
        get() = stateLiveData.value ?: ReLearnCardViewState.Initial

    init {
        stateInternal.value = ReLearnCardViewState.Initial
    }

    fun loadNthHistory(n: Int) {
        if (currentState !is ReLearnCardViewState.Initial) return
        coroutineScope.launch {
            stateInternal.postValue(ReLearnCardViewState.Loading)
            val source = getNthHistoryReLearnSourceUseCase.getNthHistoryReLearnSourceUseCase(n)
            if (source == null) {
                stateInternal.postValue(ReLearnCardViewState.NotFound)
                return@launch
            }
            val translation = getTranslationFromSourceUseCase.getTranslationFromSource(source)
            stateInternal.postValue(ReLearnCardViewState.FinishedLoading(translation))
        }
    }

    fun loadNextReLearn() {
        if (currentState !is ReLearnCardViewState.Initial) return
        coroutineScope.launch {
            stateInternal.postValue(ReLearnCardViewState.Loading)
            val source = getNextAndShowReLearnUseCase.getNextAndShowReLearnUseCase()
            if (source == null) {
                stateInternal.postValue(ReLearnCardViewState.NotFound)
                return@launch
            }
            val translation = getTranslationFromSourceUseCase.getTranslationFromSource(source)
            stateInternal.postValue(ReLearnCardViewState.FinishedLoading(translation))
        }
    }

    fun acceptReLearn() {
        val relearn =
            currentState.let { if (it is ReLearnCardViewState.FinishedLoading) it.reLearnTranslation else return }
        coroutineScope.launch {
            stateInternal.postValue(ReLearnCardViewState.Accepting(relearn))
            val newSource = acceptRelearnSourceUseCase.acceptRelearnUseCase(relearn.source)
            val translation = getTranslationFromSourceUseCase.getTranslationFromSource(newSource)
            stateInternal.postValue(ReLearnCardViewState.Accepted(translation))
        }
    }

    fun deleteReLearn(){
        val relearn =
            currentState.let { if (it is ReLearnCardViewState.FinishedLoading) it.reLearnTranslation else return }
        coroutineScope.launch {
            //this viewmodel can now be reused
            stateInternal.postValue(ReLearnCardViewState.Initial)
            setReLearnDeletedUseCase.setReLearnDeleted(relearn.source, true)
        }
    }

    fun undeleteReLearn(reLearnTranslation: ReLearnTranslation){
        stateInternal.postValue(ReLearnCardViewState.FinishedLoading(reLearnTranslation))
        coroutineScope.launch {
            setReLearnDeletedUseCase.setReLearnDeleted(reLearnTranslation.source, false)
        }
    }
}