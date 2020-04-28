package com.azyoot.relearn.di.service

import com.azyoot.relearn.service.worker.CheckAccessibilityServiceWorker
import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import dagger.Subcomponent

@ServiceScope
@Subcomponent
interface WorkerSubcomponent {
    fun inject(worker: WebpageDownloadWorker)
    fun inject(worker: CheckAccessibilityServiceWorker)
}