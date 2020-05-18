package com.azyoot.relearn.di.service

import com.azyoot.relearn.service.worker.*
import dagger.Subcomponent

@ServiceScope
@Subcomponent
interface WorkerSubcomponent {
    fun inject(worker: WebpageDownloadWorker)
    fun inject(worker: CheckAccessibilityServiceWorker)
    fun inject(worker: AcceptOrSuppressReLearnWorker)
    fun inject(worker: ReLearnWorker)
    fun inject(worker: SyncReLearnsWorker)
}