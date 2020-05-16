package com.azyoot.relearn.ui.main.relearn

import android.view.View
import com.azyoot.relearn.databinding.ItemRelearnHistoryCardBinding
import com.azyoot.relearn.di.ui.AdapterSubcomponent
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.ui.common.ReLearnTranslationFormatter
import timber.log.Timber
import javax.inject.Inject

class ReLearnHistoryCardViewHolder(
    private val viewBinding: ItemRelearnHistoryCardBinding,
    adapterComponent: AdapterSubcomponent
) :
    ReLearnBaseViewHolder(viewBinding.root) {

    @Inject
    lateinit var reLearnTranslationFormatter: ReLearnTranslationFormatter

    init {
        adapterComponent.inject(this)
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
}