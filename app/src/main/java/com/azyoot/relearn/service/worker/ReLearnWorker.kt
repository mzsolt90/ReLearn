package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.work.*
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.di.service.WorkerSubcomponent
import com.azyoot.relearn.domain.usecase.relearn.CountReLearnSourcesUseCase
import com.azyoot.relearn.domain.usecase.relearn.GetNextAndShowReLearnUseCase
import com.azyoot.relearn.domain.usecase.relearn.GetTranslationFromSourceUseCase
import com.azyoot.relearn.ui.relearn.ReLearnNotificationBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReLearnWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params
) {

    @Inject
    lateinit var getNextAndShowReLearnUseCase: GetNextAndShowReLearnUseCase

    @Inject
    lateinit var translateUseCase: GetTranslationFromSourceUseCase

    @Inject
    lateinit var countRelearnSourceUseCase: CountReLearnSourcesUseCase

    @Inject
    lateinit var notificationBuilder: ReLearnNotificationBuilder

    private val component: WorkerSubcomponent by lazy { (appContext.applicationContext as ReLearnApplication).appComponent.workerSubcomponent() }

    init {
        component.inject(this)
    }

    override suspend fun doWork(): Result {
        Timber.d("Running scheduled ReLearn worker")

        if (countRelearnSourceUseCase.countReLearnSourcesUseCase() < MIN_SOURCES_COUNT) {
            Timber.i("Not enough relearn events yet")
            return Result.success()
        }
        val nextReLearn = getNextAndShowReLearnUseCase.getNextAndShowReLearnUseCase()
        if (nextReLearn == null) {
            Timber.i("Couldn't get next relearn")
            return Result.retry()
        }
        val translation = translateUseCase.getTranslationFromSource(nextReLearn)
        if (translation.translations.isEmpty()) {
            Timber.w("No translations for ${nextReLearn.sourceText}")
            AcceptOrSuppressReLearnWorker.schedule(
                applicationContext,
                nextReLearn.latestSourceId,
                nextReLearn.sourceType,
                isSuppress = true
            )
            return Result.retry()
        }

        withContext(Dispatchers.Main) {
            notificationBuilder.createAndNotify(translation, applicationContext)
        }

        return Result.success()
    }

    companion object {
        private const val MIN_SOURCES_COUNT = 20
        private const val NAME = "ReLearnWorker"

        fun schedule(context: Context) {
            val request =
                PeriodicWorkRequestBuilder<ReLearnWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(1, TimeUnit.DAYS)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}