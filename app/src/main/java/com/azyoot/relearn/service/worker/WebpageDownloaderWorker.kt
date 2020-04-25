package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.data.repository.WebpageVisitRepository
import com.azyoot.relearn.domain.usecase.parsing.CountUnparsedWebpagesUseCase
import com.azyoot.relearn.domain.usecase.parsing.DownloadLastWebpagesAndStoreTranslationsUseCase
import com.azyoot.relearn.di.service.WorkerSubcomponent
import java.io.IOException
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
            downloadUseCase.downloadLastWebpagesAndStoreTranslations()
            if (needsReschedule()) {
                return Result.retry()
            }
        } catch (exception: IOException) {
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        const val NAME = "WebpageDownloadWorker"
    }
}