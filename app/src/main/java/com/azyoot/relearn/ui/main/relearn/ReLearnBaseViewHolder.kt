package com.azyoot.relearn.ui.main.relearn

import android.view.View
import androidx.recyclerview.widget.RecyclerView

sealed class ReLearnAction {
    object AcceptReLearn : ReLearnAction()
    object ViewReLearn : ReLearnAction()
    object AcceptAnimationFinished : ReLearnAction()
    object DeleteReLearn : ReLearnAction()
}

abstract class ReLearnBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(state: ReLearnCardViewState)

    var actionsListener: (action: ReLearnAction) -> Unit = {}
}