package com.azyoot.relearn.ui.main.relearn

import android.view.View
import com.azyoot.relearn.R
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.common.ReLearnTranslationFormatter
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import timber.log.Timber

class ReLearnHistoryCardViewHolder @AssistedInject constructor(
    private val reLearnTranslationFormatter: ReLearnTranslationFormatter,
    @Assisted private val viewBinding: ItemRelearnHistoryCardBinding
) :
    ReLearnBaseViewHolder(viewBinding.root) {

    override val isRevealed: Boolean
        get() = viewBinding.groupShowHide.visibility == View.VISIBLE

    init {
        viewBinding.card.setOnClickListener {
            actionsListener(ReLearnAction.ViewReLearn)
        }
        viewBinding.buttonDelete.setOnClickListener {
            actionsListener(ReLearnAction.DeleteReLearn)
        }
        viewBinding.showHide.setOnClickListener {
            if(isRevealed){
                viewBinding.showHide.isChecked = false
                unreveal(viewBinding.groupShowHide, viewBinding.showHide)
            } else {
                viewBinding.showHide.isChecked = true
                reveal(viewBinding.groupShowHide, viewBinding.showHide)
            }
        }
    }

    override fun bind(state: ReLearnCardViewState) {
        Timber.v("Binding history with state $state")
        when (state) {
            is ReLearnCardViewState.Loading, is ReLearnCardViewState.Initial -> {
                viewBinding.groupProgress.visibility = View.VISIBLE
                viewBinding.groupLoaded.visibility = View.GONE
            }
            is ReLearnCardViewState.ReLearnTranslationState -> {
                viewBinding.groupProgress.visibility = View.GONE
                viewBinding.groupLoaded.visibility = View.VISIBLE

                viewBinding.groupShowHide.visibility =
                    if (state.isRevealed) View.VISIBLE
                    else View.INVISIBLE

                viewBinding.showHide.isChecked = state.isRevealed

                bindTranslationData(state.reLearnTranslation)
            }
            else -> throw IllegalStateException("Invalid state $state")
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