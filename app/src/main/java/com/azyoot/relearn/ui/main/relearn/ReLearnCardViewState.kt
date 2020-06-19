package com.azyoot.relearn.ui.main.relearn

import com.azyoot.relearn.domain.entity.ReLearnTranslation

sealed class ReLearnCardReLearnState {
    object Accepting : ReLearnCardReLearnState()
    object FinishedLoading : ReLearnCardReLearnState()
    object Accepted : ReLearnCardReLearnState()
    object Deleted : ReLearnCardReLearnState()
}

sealed class ReLearnCardViewState {
    object Initial : ReLearnCardViewState()
    object Loading : ReLearnCardViewState()
    object NotFound : ReLearnCardViewState()
    data class ReLearnTranslationState(val reLearnTranslation: ReLearnTranslation,
                                       val isExpanded: Boolean,
                                       val relearnState: ReLearnCardReLearnState) : ReLearnCardViewState()
}

sealed class ReLearnCardEffect {
    abstract class ReLearnTranslationEffect(val reLearnTranslation: ReLearnTranslation) : ReLearnCardEffect()
    class Launch(reLearn: ReLearnTranslation): ReLearnTranslationEffect(reLearn)
}