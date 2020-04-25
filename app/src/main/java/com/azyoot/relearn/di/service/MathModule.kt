package com.azyoot.relearn.di.service

import dagger.Module
import dagger.Provides
import java.util.*

@Module
object MathModule {
    @Provides
    fun provideRandom(): Random = Random().apply { setSeed(System.currentTimeMillis()) }
}