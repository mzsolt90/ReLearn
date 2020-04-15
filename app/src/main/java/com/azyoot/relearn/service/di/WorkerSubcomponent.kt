package com.azyoot.relearn.service.di

import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import dagger.Subcomponent

@ServiceScope
@Subcomponent
interface WorkerSubcomponent {
    fun inject(worker: WebpageDownloadWorker)
}