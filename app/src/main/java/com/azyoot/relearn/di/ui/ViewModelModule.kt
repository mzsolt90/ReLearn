package com.azyoot.relearn.di.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azyoot.relearn.ui.common.viewmodels.BaseAndroidViewModel
import com.azyoot.relearn.ui.common.viewmodels.ViewModelsList
import com.azyoot.relearn.ui.main.MainViewModel
import com.azyoot.relearn.ui.main.cards.ReLearnCardEffect
import com.azyoot.relearn.ui.main.cards.ReLearnCardViewModel
import com.azyoot.relearn.ui.main.cards.ReLearnCardViewState
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        viewModels[modelClass]?.get() as T
}

class LifecycleScopedHolder<T>(val scopedObject: T)

class LifecycleScopedFactory @Inject constructor(private val lifecycleScopedItems: Set<LifecycleScopedHolder<Any>>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(clazz: Class<T>): T =
        lifecycleScopedItems.first { it.scopedObject.javaClass == clazz }.scopedObject as T
}

@Module(includes = [ViewModelModule.Bindings::class])
class ViewModelModule {

    @Module
    interface Bindings {
        @Binds
        fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

        @Binds
        @IntoMap
        @ViewModelKey(MainViewModel::class)
        fun mainViewModel(viewModel: MainViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(ReLearnCardViewModel::class)
        fun relearnCardViewModel(viewModel: ReLearnCardViewModel): ViewModel
    }

    @Provides
    @IntoSet
    @JvmSuppressWildcards
    fun provideRelearnViewModelList(
        viewModelsList: ViewModelsList<ReLearnCardViewState,
                ReLearnCardEffect,
                BaseAndroidViewModel<ReLearnCardViewState, ReLearnCardEffect>>
    ): LifecycleScopedHolder<Any> = LifecycleScopedHolder(viewModelsList)
}