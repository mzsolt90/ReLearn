package com.azyoot.relearn.di.core

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.time.Duration

@Module
object HttpModule {

    @Provides
    @AppScope
    fun getOkHttp() = OkHttpClient.Builder()
        .connectTimeout(Duration.ofMinutes(2))
        .readTimeout(Duration.ofMinutes(2))
        .writeTimeout(Duration.ofMinutes(2))
        .build()
}