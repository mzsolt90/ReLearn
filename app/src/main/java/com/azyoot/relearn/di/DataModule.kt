package com.azyoot.relearn.di

import android.content.Context
import androidx.room.Room
import com.azyoot.relearn.data.migration.AddGoogleTranslateMigration
import com.azyoot.relearn.data.migration.AddWebpageTranslationMigration
import com.azyoot.relearn.data.AppDatabase
import com.azyoot.relearn.data.migration.AddRelearnEventMigration
import dagger.Module
import dagger.Provides

@Module
object DataModule {

    @Provides
    fun provideRoomDatabase(applicationContext: Context) =
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "ReLearn-db")
            .addMigrations(
                AddGoogleTranslateMigration(),
                AddWebpageTranslationMigration(),
                AddRelearnEventMigration()
            ).build()
}