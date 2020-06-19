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

    override val isExpanded: Boolean
        get() = viewBinding.groupShowHide.visibility == View.VISIBLE

    init {
        viewBinding.card.setOnClickListener {
            actionsListener(ReLearnAction.ViewReLearn)
        }
        viewBinding.buttonDelete.setOnClickListener {
            actionsListener(ReLearnAction.DeleteReLearn)
        }
        viewBinding.showHide.setOnClickListener {
            actionsListener(ReLearnAction.SetExpanded(!isExpanded))
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
                viewBinding.groupShowHide.visibility = if(state.isExpanded) View.VISIBLE else View.INVISIBLE

                viewBinding.showHide.setIconResource(if(state.isExpanded) R.drawable.ic_visible else R.drawable.ic_invisible)

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