package com.azyoot.relearn.di.service

import com.azyoot.relearn.di.core.AppScope
import dagger.Module
import dagger.Provides
import java.util.*

@Module
object MathModule {
    @Provides
    @AppScope
    fun provideRandom(): Random = Random().apply { setSeed(System.currentTimeMillis()) }
}