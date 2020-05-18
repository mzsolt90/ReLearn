package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.work.*
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.di.service.WorkerSubcomponent
import com.azyoot.relearn.domain.usecase.relearn.SyncReLearnsUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncReLearnsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var syncReLearnsUseCase: SyncReLearnsUseCase

    private val component: WorkerSubcomponent by lazy { (appContext.applicationContext as ReLearnApplication).appComponent.workerSubcomponent() }

    init {
        component.inject(this)
    }

    override suspend fun doWork(): Result {
        syncReLearnsUseCase.syncReLearns()
        return Result.success()
    }

    companion object {

        private const val NAME = "SyncReLearnsWorker"

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncReLearnsWorker>()
                .setInitialDelay(2, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }

}