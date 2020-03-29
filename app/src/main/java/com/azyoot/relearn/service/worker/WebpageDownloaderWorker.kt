package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.data.WebpageVisitRepository
import com.azyoot.relearn.domain.usecase.CountUnparsedWebpagesUseCase
import com.azyoot.relearn.domain.usecase.DownloadLastWebpagesAndStoreTranslationsUseCase
import com.azyoot.relearn.service.di.WorkerSubcomponent
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

    override suspend fun doWork(): Result {
        try {
            downloadUseCase.downloadLastWebpagesAndStoreTranslations()
            if (countUseCase.countUntranslatedWebpages() > 0) {
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