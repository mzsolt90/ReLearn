package com.azyoot.relearn.di.ui

import com.azyoot.relearn.ui.main.ReLearnAdapter
import com.azyoot.relearn.ui.main.relearn.ReLearnCardViewHolder
import dagger.BindsInstance
import dagger.Component
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
    fun inject(viewHolder: ReLearnCardViewHolder)
}