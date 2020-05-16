package com.azyoot.relearn.ui.main.relearn

import android.view.View
import com.azyoot.relearn.databinding.ItemRelearnCardBinding
import com.azyoot.relearn.di.ui.AdapterSubcomponent
import com.azyoot.relearn.ui.common.ReLearnTranslationFormatter
import javax.inject.Inject

class ReLearnNextCardViewHolder(
    private val viewBinding: ItemRelearnCardBinding,
    adapterComponent: AdapterSubcomponent
) :
    ReLearnBaseViewHolder(viewBinding.root) {

    @Inject
    lateinit var reLearnTranslationFormatter: ReLearnTranslationFormatter

    init {
        adapterComponent.inject(this)

        viewBinding.buttonAccept.setOnClickListener { actionsInternal.postValue(ReLearnAction.AcceptReLearn) }
        viewBinding.buttonView.setOnClickListener { actionsInternal.postValue(ReLearnAction.ViewReLearn) }
    }

    override fun bind(state: ReLearnCardViewState) {
        when (state) {
            is ReLearnCardViewState.Loading, is ReLearnCardViewState.Initial -> {
                viewBinding.groupProgress.visibility = View.VISIBLE
                viewBinding.groupLoaded.visibility = View.GONE
            }
            is ReLearnCardViewState.Finished -> {
                viewBinding.groupProgress.visibility = View.GONE
                viewBinding.groupLoaded.visibility = View.VISIBLE
                bindTranslationData(state)
            }
        }
    }

    private fun bindTranslationData(state: ReLearnCardViewState.Finished) {
        viewBinding.sourceTitle.text = state.reLearnTranslation.sourceText
        viewBinding.sourceTranslation.text =
            reLearnTranslationFormatter.formatTranslationTextForNotification(state.reLearnTranslation)
    }
}