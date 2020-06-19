package com.azyoot.relearn.ui.main.relearn

import com.azyoot.relearn.domain.entity.ReLearnTranslation

sealed class ReLearnCardViewState {
    object Initial : ReLearnCardViewState()
    object Loading : ReLearnCardViewState()
    object NotFound : ReLearnCardViewState()
    abstract class ReLearnTranslationState(val reLearnTranslation: ReLearnTranslation) : ReLearnCardViewState()
    class Accepting(reLearn: ReLearnTranslation) : ReLearnTranslationState(reLearn)
    class FinishedLoading(reLearn: ReLearnTranslation) : ReLearnTranslationState(reLearn)
    class Accepted(reLearn: ReLearnTranslation) : ReLearnTranslationState(reLearn)
    class Deleted(reLearn: ReLearnTranslation) : ReLearnTranslationState(reLearn)
}

sealed class ReLearnCardEffect {
    abstract class ReLearnTranslationEffect(val reLearnTranslation: ReLearnTranslation) : ReLearnCardEffect()
    class Launch(reLearn: ReLearnTranslation): ReLearnTranslationEffect(reLearn)
}