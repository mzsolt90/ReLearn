package com.azyoot.relearn.di.service

import com.azyoot.relearn.di.service.ServiceScope
import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import dagger.Subcomponent

@ServiceScope
@Subcomponent
interface WorkerSubcomponent {
    fun inject(worker: WebpageDownloadWorker)
}