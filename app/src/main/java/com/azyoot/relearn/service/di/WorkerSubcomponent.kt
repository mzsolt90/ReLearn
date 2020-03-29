package com.azyoot.relearn.service.di

import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import dagger.Subcomponent

@Subcomponent(modules = [HttpModule::class])
interface WorkerSubcomponent {
    fun inject(worker: WebpageDownloadWorker)
}