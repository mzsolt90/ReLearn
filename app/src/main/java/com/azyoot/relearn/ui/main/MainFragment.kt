package com.azyoot.relearn.ui.main

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.azyoot.relearn.R
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.databinding.FragmentMainBinding
import com.azyoot.relearn.di.ui.MainFragmentSubcomponent
import com.azyoot.relearn.domain.config.MIN_SOURCES_COUNT
import com.azyoot.relearn.service.common.ReLearnLauncher
import com.azyoot.relearn.service.worker.CheckAccessibilityServiceWorker
import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import com.azyoot.relearn.ui.animation.AnimatedTumbleweed
import com.azyoot.relearn.ui.common.runOnAnimationEnd
import com.azyoot.relearn.ui.common.runOnAnimationStart
import com.azyoot.relearn.ui.main.cards.ReLearnCardViewState
import com.azyoot.relearn.ui.onboarding.OnboardingFragment
import com.azyoot.relearn.ui.onboarding.OnboardingFragmentParams
import com.azyoot.relearn.util.dpToPx
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var relearnLauncher: ReLearnLauncher

    @Inject
    lateinit var relearnAdapterFactory: ReLearnAdapter.Factory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }
    private var viewBinding: FragmentMainBinding? = null

    private val component: MainFragmentSubcomponent by lazy {
        (requireContext().applicationContext as ReLearnApplication).appComponent
            .mainFragmentSubcomponentFactory()
            .create(lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        FragmentMainBinding.inflate(LayoutInflater.from(context)).let { binding ->
            this.viewBinding = binding
            binding.root
        }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rescheduleWebpageDownloadWorker()
        scheduleAccessibilityServiceCheckWorker()
        scheduleReLearn()

        viewBinding!!.refresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewBinding!!.fab.setOnClickListener {
            viewBinding!!.relearnPager.setCurrentItem(
                viewBinding!!.relearnPager.adapter?.itemCount ?: 1 - 1, true
            )
        }

        viewModel.getViewState()
            .onEach { bindState(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.getEffects().onEach {
            when (it) {
                MainViewEffect.EnableAccessibilityService -> showServiceNotEnabledWarning()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onResume() {
        super.onResume()

        if (viewBinding!!.groupEmpty.visibility == View.VISIBLE) {
            viewModel.refresh()
        }
    }

    private fun isAlreadyBound(viewState: MainViewState.Loaded): Boolean {
        val adapter = viewBinding!!.relearnPager.adapter
        if (adapter !is ReLearnAdapter) return false
        if (adapter.itemCount > viewState.sourceCount) return false
        return viewBinding!!.relearnPager.currentItem == viewState.page
    }

    private fun bindState(viewState: MainViewState) {
        when (viewState) {
            is MainViewState.Loading, is MainViewState.Initial -> {
                viewBinding!!.groupProgress.visibility = View.VISIBLE
                viewBinding!!.groupLoaded.visibility = View.GONE
                viewBinding!!.groupEmpty.visibility = View.GONE

                viewBinding!!.relearnPager.adapter = null
            }
            is MainViewState.Loaded -> {
                Timber.d("Loaded main view state $viewState")
                viewBinding!!.refresh.isRefreshing = false
                viewBinding!!.groupProgress.visibility = View.GONE

                if (viewState.sourceCount <= MIN_SOURCES_COUNT) {
                    viewBinding!!.groupEmpty.visibility = View.VISIBLE
                    viewBinding!!.groupLoaded.visibility = View.GONE
                    showEmptyState(viewState)
                } else if (!isAlreadyBound(viewState)) {
                    viewBinding!!.groupEmpty.visibility = View.GONE
                    viewBinding!!.groupLoaded.visibility = View.VISIBLE
                    setupViewPager(viewState)
                }
            }
            is MainViewState.Onboarding -> bindOnboardingState(viewState)
        }

        if (viewState is MainViewState.Onboarding) {
            (activity as AppCompatActivity).supportActionBar?.hide()
        } else {
            (activity as AppCompatActivity).supportActionBar?.show()
        }

        if (currentOnboardingScreen() != null && viewState !is MainViewState.Onboarding) {
            hideOnboardingFragment()
        }

        updateFabVisibility()
    }

    private fun currentOnboardingScreen() =
        (childFragmentManager.findFragmentByTag(OnboardingFragment.TAG) as? OnboardingFragment?)
            ?.getParams()
            ?.screen

    private fun showOnboardingFragment(params: OnboardingFragmentParams) {
        childFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.main, OnboardingFragment.newInstance(params), OnboardingFragment.TAG)
            .commit()
    }

    private fun hideOnboardingFragment() {
        val currentFragment =
            childFragmentManager.findFragmentByTag(OnboardingFragment.TAG) ?: return
        childFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in, R.anim.slide_out_down, android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .remove(currentFragment)
            .commit()
    }

    private fun bindOnboardingState(state: MainViewState.Onboarding) {
        viewBinding!!.groupProgress.visibility = View.GONE
        viewBinding!!.groupLoaded.visibility = View.GONE
        viewBinding!!.groupEmpty.visibility = View.GONE

        if (currentOnboardingScreen() != state.screen) {
            showOnboardingFragment(OnboardingFragmentParams((state.screen)))
        }
    }

    private fun showServiceNotEnabledWarning() {
        MaterialAlertDialogBuilder(activity)
            .setMessage(R.string.dialog_enable_accessibility_service)
            .setNegativeButton(android.R.string.no) { _, _ -> }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .show()
    }

    private fun showEmptyState(viewState: MainViewState.Loaded) {
        viewBinding!!.apply {
            groupEmpty.visibility = View.VISIBLE
            groupLoaded.visibility = View.GONE
            groupProgress.visibility = View.GONE

            emptySubtitle.text = resources.getString(
                R.string.message_empty_remaining,
                MIN_SOURCES_COUNT - viewState.sourceCount
            )

            emptyImage.apply {
                visibility = View.VISIBLE
                val anim = AnimatedTumbleweed(context)
                setImageDrawable(anim)
                anim.start()
            }
        }
    }

    private fun setupViewPager(viewState: MainViewState.Loaded) {
        val relearnAdapter = relearnAdapterFactory.create(
            viewState.sourceCount,
            this,
            viewLifecycleOwner.lifecycleScope
        )

        Timber.d("Creating new view pager")

        if (viewBinding!!.relearnPager.adapter == null) {
            viewBinding!!.relearnPager.apply {
                adapter = relearnAdapter
                setCurrentItem(viewState.page, false)

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        Timber.v("Page selected $position")
                        viewModel.onPageChanged(position)

                        updateFabVisibility()
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        viewBinding?.refresh?.apply {
                            isEnabled = state == ViewPager2.SCROLL_STATE_IDLE
                        }
                    }
                })
            }
        } else {
            viewBinding!!.relearnPager.adapter = relearnAdapter
        }

        relearnAdapter.getEffects().onEach {
            when (it) {
                is ReLearnAdapterEffect.LaunchReLearnEffect -> {
                    relearnLauncher.launch(it.reLearnTranslation)
                }
                is ReLearnAdapterEffect.ShowNextReLearnEffect -> {
                    viewBinding!!.relearnPager.post {
                        viewBinding!!.relearnPager.setCurrentItem(
                            relearnAdapter.itemCount - 1,
                            true
                        )
                    }
                }
                is ReLearnAdapterEffect.ReLearnDeletedEffect -> {
                    onReLearnDeleted(it.position, it.state)
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun animateShowFab() {
        ObjectAnimator.ofFloat(
            viewBinding!!.fab,
            "translationY",
            requireContext().dpToPx(80),
            0F
        ).apply {
            duration = 300

            viewLifecycleOwner.lifecycleScope.runOnAnimationStart(this) {
                viewBinding?.fab?.visibility = View.VISIBLE
            }

            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun animateHideFab() {
        ObjectAnimator.ofFloat(
            viewBinding!!.fab,
            "translationY",
            0F,
            requireContext().dpToPx(80)
        ).apply {
            duration = 300

            viewLifecycleOwner.lifecycleScope.runOnAnimationEnd(this) {
                viewBinding?.fab?.visibility = View.GONE
            }

            interpolator = AccelerateInterpolator()
            start()
        }
    }

    private fun updateFabVisibility() {
        if (viewBinding!!.groupLoaded.visibility != View.VISIBLE) {
            viewBinding!!.fab.visibility = View.GONE
            return
        }

        val visible =
            viewBinding!!.relearnPager.currentItem < viewBinding!!.relearnPager.adapter!!.itemCount - 2
        if (visible && viewBinding!!.fab.visibility != View.VISIBLE) {
            animateShowFab()
        } else if (!visible && viewBinding!!.fab.visibility != View.GONE) {
            animateHideFab()
        }
    }

    private fun vibrate() {
        val vibratorService =
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator? ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibratorService.vibrate(
                VibrationEffect.createOneShot(
                    500,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibratorService.vibrate(500)
        }
    }

    private fun onReLearnDeleted(
        position: Int,
        state: ReLearnCardViewState.ReLearnTranslationState
    ) {
        vibrate()

        viewBinding!!.snackbarManager.show(
            text = getString(R.string.message_relearn_deleted, state.reLearnTranslation.sourceText),
            actionTextRes = R.string.action_undo,
            duration = TimeUnit.SECONDS.toMillis(5)
        ) {
            (viewBinding?.relearnPager?.adapter as? ReLearnAdapter)?.undoReLearnDelete(
                position,
                state
            )
            viewBinding?.relearnPager?.setCurrentItem(position, true)
        }
    }

    private fun rescheduleWebpageDownloadWorker() {
        WebpageDownloadWorker.schedule(requireContext().applicationContext, 0)
    }

    private fun scheduleAccessibilityServiceCheckWorker() {
        CheckAccessibilityServiceWorker.schedule(requireContext().applicationContext)
    }

    private fun scheduleReLearn() {
        viewModel.scheduleReLearn()
    }
}
