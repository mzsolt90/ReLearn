package com.azyoot.relearn.di.service

import com.azyoot.relearn.domain.usecase.monitoring.BrowserUrlBarFlagAndSaveDataUseCase
import com.azyoot.relearn.domain.usecase.monitoring.FlagAndSaveEventDataUseCase
import com.azyoot.relearn.domain.usecase.monitoring.GoogleTranslateFlagAndSaveDataUseCase
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
interface ServiceUseCasesModule {

    @Binds
    @IntoSet
    fun provideUrlBarUseCase(browserUrlBarFlagAndSaveDataUseCase: BrowserUrlBarFlagAndSaveDataUseCase): FlagAndSaveEventDataUseCase

    @Binds
    @IntoSet
    fun provideGoogleTranslateUseCase(googleTranslateFlagAndSaveDataUseCase: GoogleTranslateFlagAndSaveDataUseCase): FlagAndSaveEventDataUseCase
}