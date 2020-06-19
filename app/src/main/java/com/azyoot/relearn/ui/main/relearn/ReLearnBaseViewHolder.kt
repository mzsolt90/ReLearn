package com.azyoot.relearn.ui.main.relearn

import android.view.View
import androidx.recyclerview.widget.RecyclerView

sealed class ReLearnAction {
    object AcceptReLearn : ReLearnAction()
    object ViewReLearn : ReLearnAction()
    object AcceptAnimationFinished : ReLearnAction()
    object DeleteReLearn : ReLearnAction()
    data class SetExpanded(val isExpanded: Boolean) : ReLearnAction()
}

abstract class ReLearnBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(state: ReLearnCardViewState)

    abstract val isExpanded: Boolean

    var actionsListener: (action: ReLearnAction) -> Unit = {}
}