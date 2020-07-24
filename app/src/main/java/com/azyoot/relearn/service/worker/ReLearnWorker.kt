package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.di.service.WorkerSubcomponent
import com.azyoot.relearn.domain.config.MIN_SOURCES_COUNT
import com.azyoot.relearn.domain.usecase.relearn.CountReLearnSourcesUseCase
import com.azyoot.relearn.domain.usecase.relearn.GetNextAndShowReLearnUseCase
import com.azyoot.relearn.domain.usecase.relearn.GetTranslationFromSourceUseCase
import com.azyoot.relearn.ui.notification.ID_RELEARN
import com.azyoot.relearn.ui.notification.ReLearnNotificationFactory
import com.azyoot.relearn.ui.notification.ensureChannelCreated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
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
    lateinit var notificationFactory: ReLearnNotificationFactory

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
            ensureChannelCreated(applicationContext)

            notificationFactory.create(translation).also {
                NotificationManagerCompat.from(applicationContext).notify(ID_RELEARN, it)
            }
        }

        return Result.success()
    }

    companion object {
        private const val NAME = "ReLearnWorker"

        fun run(context: Context) {
            val request =
                OneTimeWorkRequestBuilder<ReLearnWorker>()
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}