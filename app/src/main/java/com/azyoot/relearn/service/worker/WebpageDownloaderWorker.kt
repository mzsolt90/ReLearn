package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.work.*
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.usecase.parsing.CountUnparsedWebpagesUseCase
import com.azyoot.relearn.domain.usecase.parsing.DownloadLastWebpagesAndStoreTranslationsUseCase
import com.azyoot.relearn.di.service.WorkerSubcomponent
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WebpageDownloadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var repository: WebpageVisitRepository

    @Inject
    lateinit var downloadUseCase: DownloadLastWebpagesAndStoreTranslationsUseCase

    @Inject
    lateinit var countUseCase: CountUnparsedWebpagesUseCase

    private val component: WorkerSubcomponent by lazy { (appContext.applicationContext as ReLearnApplication).appComponent.workerSubcomponent() }

    init {
        component.inject(this)
    }

    private suspend fun needsReschedule() = countUseCase.countUntranslatedWebpages() > 0
        
    override suspend fun doWork(): Result {
        try {
            Timber.i("Webpage download started")
            downloadUseCase.downloadLastWebpagesAndStoreTranslations()
            if (needsReschedule()) {
                Timber.i("Will reschedule webpage download")
                schedule(applicationContext, RESCHEDULE_SECONDS)
                return Result.success()
            } else {
                SyncReLearnsWorker.schedule(applicationContext)
            }
        } catch (exception: IOException) {
            Timber.e(exception, "Error downloading webpages")
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        const val NAME = "WebpageDownloadWorker"
        private const val RESCHEDULE_SECONDS = 5

        fun schedule(context: Context, initialDelaySeconds: Int = 60) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<WebpageDownloadWorker>()
                .setConstraints(constraints)
                .setInitialDelay(initialDelaySeconds.toLong(), TimeUnit.SECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}