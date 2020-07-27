package com.azyoot.relearn.di.core

import android.content.Context
import androidx.room.Room
import com.azyoot.relearn.data.AppDatabase
import com.azyoot.relearn.data.dao.RelearnEventDataHandler
import com.azyoot.relearn.data.migration.*
import dagger.Module
import dagger.Provides

@Module
object DataModule {

    @Provides
    @AppScope
    fun provideRoomDatabase(applicationContext: Context): AppDatabase =
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "ReLearn-db")
            .addMigrations(
                AddGoogleTranslateMigration(),
                AddWebpageTranslationMigration(),
                AddRelearnEventMigration(),
                AddLatestSourcesViewMigration(),
                UpdateLatestSourcesViewMigration(),
                BumpParseVersionToRefreshWebpageTranslationsMigration(),
                NoMultipleWebpageVisitsForSameUrlMigration(),
                SoftDeletionSupportMigration()
            ).build()

    @Provides
    @AppScope
    fun provideRelearnEventDataHandler(appDatabase: AppDatabase): RelearnEventDataHandler =
        appDatabase.relearnEventDao()
}