package com.azyoot.relearn.service.di

import com.azyoot.relearn.domain.usecase.ChromeUrlBarFlagAndSaveDataUseCase
import com.azyoot.relearn.domain.usecase.FlagAndSaveEventDataUseCase
import com.azyoot.relearn.domain.usecase.GoogleTranslateFlagAndSaveDataUseCase
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
interface UseCasesModule {

    @Binds
    @IntoSet
    fun provideChromeUrlBarUseCase(chromeUrlBarFlagAndSaveDataUseCase: ChromeUrlBarFlagAndSaveDataUseCase): FlagAndSaveEventDataUseCase

    @Binds
    @IntoSet
    fun provideGoogleTranslateUseCase(googleTranslateFlagAndSaveDataUseCase: GoogleTranslateFlagAndSaveDataUseCase): FlagAndSaveEventDataUseCase
}