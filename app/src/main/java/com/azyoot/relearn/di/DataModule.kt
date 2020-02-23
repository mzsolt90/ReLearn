package com.azyoot.relearn.di

import android.content.Context
import androidx.room.Room
import com.azyoot.relearn.data.AppDatabase
import dagger.Module
import dagger.Provides

@Module
object DataModule {

    @Provides
    fun provideRoomDatabase(applicationContext: Context) = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "ReLearn-db").build()
}