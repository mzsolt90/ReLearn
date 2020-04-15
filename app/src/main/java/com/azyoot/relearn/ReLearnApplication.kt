package com.azyoot.relearn

import android.app.Application
import com.azyoot.relearn.di.ApplicationComponent
import com.azyoot.relearn.di.DaggerApplicationComponent
import com.facebook.stetho.Stetho

class ReLearnApplication : Application() {
    lateinit var appComponent: ApplicationComponent;

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerApplicationComponent.factory().create(this)
        Stetho.initializeWithDefaults(this);
    }
}