package com.azyoot.relearn.di.ui

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(includes = [AssistedInject_AdapterModule::class])
abstract class AdapterModule {

}