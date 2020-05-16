package com.azyoot.relearn.ui.main.relearn

import com.azyoot.relearn.domain.entity.ReLearnTranslation

sealed class ReLearnCardViewState {
    object Initial : ReLearnCardViewState()
    object Loading : ReLearnCardViewState()
    object NotFound : ReLearnCardViewState()
    abstract class ReLearnTranslationState(val reLearnTranslation: ReLearnTranslation) : ReLearnCardViewState()
    class Accepting(reLearnTranslation: ReLearnTranslation) : ReLearnTranslationState(reLearnTranslation)
    class FinishedLoading(reLearnTranslation: ReLearnTranslation) : ReLearnTranslationState(reLearnTranslation)
    class Accepted(reLearnTranslation: ReLearnTranslation) : ReLearnTranslationState(reLearnTranslation)
}