package com.azyoot.relearn.ui.main.cards

import android.view.View
import androidx.recyclerview.widget.RecyclerView

sealed class ReLearnAction {
    object AcceptReLearn : ReLearnAction()
    object ViewReLearn : ReLearnAction()
    object AcceptAnimationFinished : ReLearnAction()
    object DeleteReLearn : ReLearnAction()
    data class SetExpanded(val isExpanded: Boolean) : ReLearnAction()
}

abstract class ReLearnCardBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var actionsListener: (action: ReLearnAction) -> Unit = {}

    private var boundState: ReLearnCardViewState = ReLearnCardViewState.Initial
    protected val isRevealed: Boolean
        get() = boundState.let { it is ReLearnCardViewState.ReLearnTranslationState && it.isRevealed }

    fun bind(state: ReLearnCardViewState) {
        bind(state, boundState)
        boundState = state
    }

    abstract fun bind(newState: ReLearnCardViewState, oldState: ReLearnCardViewState)
    fun unbind(){
        boundState = ReLearnCardViewState.Initial
    }
}