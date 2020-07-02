package com.azyoot.relearn.ui.main.relearn

import android.view.View
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.animation.GroupRevealAnimator
import com.azyoot.relearn.ui.common.ReLearnTranslationFormatter
import com.azyoot.relearn.util.setAlphaProper
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber

class ReLearnHistoryCardViewHolder @AssistedInject constructor(
    private val reLearnTranslationFormatter: ReLearnTranslationFormatter,
    private val groupRevealAnimator: GroupRevealAnimator,
    @Assisted private val viewBinding: ItemRelearnHistoryCardBinding
) :
    ReLearnBaseViewHolder(viewBinding.root) {

    init {
        viewBinding.card.setOnClickListener {
            actionsListener(ReLearnAction.ViewReLearn)
        }
        viewBinding.buttonDelete.setOnClickListener {
            actionsListener(ReLearnAction.DeleteReLearn)
        }
        viewBinding.showHide.setOnClickListener {
            actionsListener(ReLearnAction.SetExpanded(!isRevealed))
        }
    }

    private fun needsRevealAnimation(
        newState: ReLearnCardViewState,
        oldState: ReLearnCardViewState
    ) = when {
        newState !is ReLearnCardViewState.ReLearnTranslationState -> false
        oldState !is ReLearnCardViewState.ReLearnTranslationState -> false
        newState.isRevealed == oldState.isRevealed -> false
        else -> true
    }

    override fun bind(newState: ReLearnCardViewState, oldState: ReLearnCardViewState) {
        Timber.v("Binding history with state $newState")
        when (newState) {
            is ReLearnCardViewState.Loading, is ReLearnCardViewState.Initial -> {
                viewBinding.groupProgress.visibility = View.VISIBLE
                viewBinding.groupLoaded.visibility = View.GONE
            }
            is ReLearnCardViewState.ReLearnTranslationState -> {
                viewBinding.groupProgress.visibility = View.GONE
                viewBinding.groupLoaded.visibility = View.VISIBLE

                bindTranslationData(newState.reLearnTranslation)
                bindRevealState(newState, oldState)
            }
            else -> throw IllegalStateException("Invalid state $newState")
        }
    }

    private fun bindRevealState(
        newState: ReLearnCardViewState.ReLearnTranslationState,
        oldState: ReLearnCardViewState
    ) {
        if (viewBinding.showHide.isChecked != newState.isRevealed) {
            viewBinding.showHide.isChecked = newState.isRevealed
        }
        if (needsRevealAnimation(newState, oldState)) {
            if (newState.isRevealed) {
                groupRevealAnimator.reveal(viewBinding.groupShowHide, viewBinding.showHide)
            } else {
                groupRevealAnimator.unreveal(viewBinding.groupShowHide, viewBinding.showHide)
            }
        } else {
            viewBinding.groupShowHide.setAlphaProper(if (newState.isRevealed) 1F else 0F)
        }
    }

    private fun bindTranslationData(reLearnTranslation: ReLearnTranslation) {
        viewBinding.sourceTitle.text = reLearnTranslation.sourceText
        viewBinding.sourceTranslation.text =
            reLearnTranslationFormatter.formatTranslationTextForNotification(reLearnTranslation)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(viewBinding: ItemRelearnHistoryCardBinding): ReLearnHistoryCardViewHolder
    }
}