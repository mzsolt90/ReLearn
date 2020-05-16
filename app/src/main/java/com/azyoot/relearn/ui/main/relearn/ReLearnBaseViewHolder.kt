package com.azyoot.relearn.ui.main.relearn

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

sealed class ReLearnAction{
    object AcceptReLearn : ReLearnAction()
    object ViewReLearn : ReLearnAction()
}

abstract class ReLearnBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(state: ReLearnCardViewState)

    protected val actionsInternal = MutableLiveData<ReLearnAction>()
    val actionsLiveData: LiveData<ReLearnAction>
    get() = actionsInternal
}