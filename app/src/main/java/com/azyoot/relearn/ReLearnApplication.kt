package com.azyoot.relearn

import android.app.Application
import android.util.Log
import com.azyoot.relearn.di.core.ApplicationComponent
import com.azyoot.relearn.di.core.DaggerApplicationComponent
import com.crashlytics.android.Crashlytics
import timber.log.Timber

import timber.log.Timber.DebugTree

class ReLearnApplication : Application() {
    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerApplicationComponent.factory().create(this)

        if(BuildConfig.DEBUG) {
            com.facebook.stetho.Stetho.initializeWithDefaults(this)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}

private class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        Crashlytics.log(priority, tag, message)

        if (t != null) {
            if (priority == Log.ERROR || priority == Log.WARN) {
                Crashlytics.logException(t);
            }
        }
    }

}