package com.azyoot.relearn.service.worker

import android.content.Context
import androidx.work.*
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.di.service.WorkerSubcomponent
import com.azyoot.relearn.domain.entity.ReLearnSource
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.domain.usecase.relearn.AcceptRelearnSourceUseCase
import com.azyoot.relearn.domain.usecase.relearn.GetReLearnSourceFromIdUseCase
import com.azyoot.relearn.domain.usecase.relearn.SuppressRelearnSourceUseCase
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AcceptOrSuppressReLearnWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var acceptRelearnSourceUseCase: AcceptRelearnSourceUseCase

    @Inject
    lateinit var suppressReLearnSourceUseCase: SuppressRelearnSourceUseCase

    @Inject
    lateinit var getRelearnSourceFromIdUseCase: GetReLearnSourceFromIdUseCase

    private val component: WorkerSubcomponent by lazy { (appContext.applicationContext as ReLearnApplication).appComponent.workerSubcomponent() }

    init {
        component.inject(this)
    }

    override suspend fun doWork(): Result {
        val id = inputData.getLong(EXTRA_SOURCE_ID, -1)
        val type = inputData.getInt(EXTRA_SOURCE_TYPE, -1)
            .let { intValue -> SourceType.values().firstOrNull { it.value == intValue } }
        if (id < 0 || type == null) {
            Timber.w("Invalid input data given")
            return Result.failure()
        }

        val isAccept = inputData.getBoolean(EXTRA_ACCEPT, false)
        val isSuppress = inputData.getBoolean(EXTRA_SUPPRESS, false)
        if (!isAccept && !isSuppress) {
            Timber.w("Invalid input data given: nor accept, nor suppress")
            return Result.failure()
        }

        val source = getRelearnSourceFromIdUseCase.getReLearnSourceFromIdUseCase(id, type)
        if (source == null) {
            Timber.w("Cannot find source with id $id and type $type")
            return Result.failure()
        }

        if (isAccept) {
            acceptRelearnSourceUseCase.acceptRelearnUseCase(source)
        } else if (isSuppress) {
            suppressReLearnSourceUseCase.suppressRelearnSource(source)
        }

        return Result.success()
    }

    companion object {
        const val EXTRA_ACCEPT = "accept"
        const val EXTRA_SUPPRESS = "suppress"
        const val EXTRA_SOURCE_ID = "source_id"
        const val EXTRA_SOURCE_TYPE = "source_type"

        fun schedule(
            context: Context,
            sourceId: Long,
            sourceType: SourceType,
            isAccept: Boolean = false,
            isSuppress: Boolean = false
        ) {
            if (!isAccept && !isSuppress) throw IllegalArgumentException("Either isAccept of isSuppress has to be true")

            val request = OneTimeWorkRequestBuilder<AcceptOrSuppressReLearnWorker>()
                .setInputData(
                    workDataOf(
                        EXTRA_ACCEPT to isAccept,
                        EXTRA_SUPPRESS to isSuppress,
                        EXTRA_SOURCE_ID to sourceId,
                        EXTRA_SOURCE_TYPE to sourceType.value
                    )
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueue(request)
        }
    }
}