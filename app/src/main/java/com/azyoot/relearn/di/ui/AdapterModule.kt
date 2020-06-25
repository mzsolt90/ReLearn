package com.azyoot.relearn.di.ui

import com.azyoot.relearn.ui.common.BaseAndroidViewModel
import com.azyoot.relearn.ui.common.ViewModelsList
import com.azyoot.relearn.ui.main.relearn.ReLearnCardEffect
import com.azyoot.relearn.ui.main.relearn.ReLearnCardViewState
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@AssistedModule
@Module(includes = [AssistedInject_AdapterModule::class])
interface AdapterModule