package com.azyoot.relearn.ui.main.relearn

import com.azyoot.relearn.domain.entity.ReLearnTranslation

sealed class ReLearnCardViewState {
    object Initial : ReLearnCardViewState()
    object Loading : ReLearnCardViewState()
    object NotFound : ReLearnCardViewState()
    data class Finished(val reLearnTranslation: ReLearnTranslation) : ReLearnCardViewState()
}