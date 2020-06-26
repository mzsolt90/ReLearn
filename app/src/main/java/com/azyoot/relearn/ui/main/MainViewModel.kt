package com.azyoot.relearn.ui.main

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.azyoot.relearn.domain.config.PREFERENCES_NAME
import com.azyoot.relearn.domain.config.PREF_ONBOARDING_SEEN
import com.azyoot.relearn.domain.usecase.relearn.CountReLearnSourcesUseCase
import com.azyoot.relearn.domain.usecase.relearn.SyncReLearnsUseCase
import com.azyoot.relearn.service.MonitoringService
import com.azyoot.relearn.ui.common.BaseAndroidViewModel
import com.azyoot.relearn.ui.onboarding.OnboardingScreen
import com.azyoot.relearn.ui.relearn.ReLearnPeriodicScheduler
import kotlinx.coroutines.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel @Inject constructor(
    private val applicationContext: Context,
    private val reLearnPeriodicScheduler: ReLearnPeriodicScheduler,
    private val countReLearnSourcesUseCase: CountReLearnSourcesUseCase,
    private val syncReLearnsUseCase: SyncReLearnsUseCase
) : BaseAndroidViewModel<MainViewState, MainViewEffect>(MainViewState.Initial) {

    init {
        if (isOnboardingCompleted) {
            checkAccessibilityService()
            loadData()
        } else {
            startOnboarding()
        }
        scheduleReLearn()
    }

    private fun checkAccessibilityService() {
        if (!MonitoringService.isRunning(applicationContext)) {
            coroutineScope.launch {
                sendEffect(MainViewEffect.EnableAccessibilityService)
            }
        }
    }

    private val isOnboardingCompleted: Boolean
        get() = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(
                PREF_ONBOARDING_SEEN, false
            )

    private fun loadData() {
        viewState.value = MainViewState.Loading

        viewModelScope.launch {
            val count = countReLearnSourcesUseCase.countReLearnSourcesUseCase()
            withContext(Dispatchers.Main) {
                viewState.value =
                    MainViewState.Loaded(
                        count,
                        getDefaultPage(count)
                    )
            }
        }
    }

    private fun startOnboarding() {
        viewState.value = MainViewState.Onboarding(OnboardingScreen.WELCOME)
    }

    fun scheduleReLearn() {
        reLearnPeriodicScheduler.schedule()
    }

    fun refresh() {
        coroutineScope.launch {
            val previousPage = (currentViewState as? MainViewState.Loaded)?.page
            viewState.value = MainViewState.Loading

            syncReLearnsUseCase.syncReLearns()
            val count = countReLearnSourcesUseCase.countReLearnSourcesUseCase()

            withContext(Dispatchers.Main) {
                viewState.value =
                    MainViewState.Loaded(
                        count,
                        previousPage?.let { if (it >= count) null else it } ?: getDefaultPage(count)
                    )
            }
        }
    }

    private fun getDefaultPage(relearnCount: Int) = relearnCount

    fun onPageChanged(page: Int) {
        currentViewState.let {
            if (it !is MainViewState.Loaded) return@let
            if (it.page == page) return
            viewState.value = it.copy(page = page)
        }
    }

    fun setOnboardingSeen(){
        applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(
                PREF_ONBOARDING_SEEN, true
            )
            .apply()
    }

    fun onOnboardingScreenNext(screen: OnboardingScreen) {
        currentViewState.also {
            if(it !is MainViewState.Onboarding) return

            when(it.screen){
                OnboardingScreen.WELCOME -> viewState.value = MainViewState.Onboarding(OnboardingScreen.HOW_IT_WORKS)
                OnboardingScreen.HOW_IT_WORKS -> viewState.value = MainViewState.Onboarding(OnboardingScreen.ENABLE_ACCESSIBILITY)
                OnboardingScreen.ENABLE_ACCESSIBILITY -> viewState.value = MainViewState.Onboarding(OnboardingScreen.DONE)
                OnboardingScreen.DONE -> {
                    setOnboardingSeen()
                    loadData()
                }
            }
        }

    }

    fun onOnboardingScreenClosed(screen: OnboardingScreen) {
        setOnboardingSeen()
        loadData()
    }
}
