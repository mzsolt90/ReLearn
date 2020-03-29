package com.azyoot.relearn.service.di

import com.azyoot.relearn.domain.usecase.monitoring.ChromeUrlBarFlagAndSaveDataUseCase
import com.azyoot.relearn.domain.usecase.monitoring.FlagAndSaveEventDataUseCase
import com.azyoot.relearn.domain.usecase.monitoring.GoogleTranslateFlagAndSaveDataUseCase
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
interface ServiceUseCasesModule {

    @Binds
    @IntoSet
    fun provideChromeUrlBarUseCase(chromeUrlBarFlagAndSaveDataUseCase: ChromeUrlBarFlagAndSaveDataUseCase): FlagAndSaveEventDataUseCase

    @Binds
    @IntoSet
    fun provideGoogleTranslateUseCase(googleTranslateFlagAndSaveDataUseCase: GoogleTranslateFlagAndSaveDataUseCase): FlagAndSaveEventDataUseCase
}