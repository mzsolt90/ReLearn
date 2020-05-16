package com.azyoot.relearn.di.ui

import com.azyoot.relearn.ui.main.ReLearnAdapter
import com.azyoot.relearn.ui.main.relearn.ReLearnHistoryCardViewHolder
import com.azyoot.relearn.ui.main.relearn.ReLearnNextCardViewHolder
import dagger.BindsInstance
import dagger.Subcomponent
import kotlinx.coroutines.CoroutineScope

@Subcomponent
@AdapterScope
interface AdapterSubcomponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(@BindsInstance @AdapterScope coroutineScope: CoroutineScope): AdapterSubcomponent
    }

    fun inject(adapter: ReLearnAdapter)
    fun inject(viewHolder: ReLearnHistoryCardViewHolder)
    fun inject(viewHolder: ReLearnNextCardViewHolder)
}