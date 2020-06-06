package com.azyoot.relearn.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.azyoot.relearn.R
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.databinding.FragmentMainBinding
import com.azyoot.relearn.di.ui.MainFragmentSubcomponent
import com.azyoot.relearn.domain.config.MIN_SOURCES_COUNT
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.service.common.ReLearnLauncher
import com.azyoot.relearn.service.worker.CheckAccessibilityServiceWorker
import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import com.azyoot.relearn.ui.animation.AnimatedTumbleweed
import com.azyoot.relearn.util.dpToPx
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
        (context!!.applicationContext as ReLearnApplication).appComponent
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
            viewBinding!!.relearnPager.setCurrentItem(viewBinding!!.relearnPager.adapter?.itemCount ?: 1 - 1, true)
        }

        viewModel.state()
            .onEach { bindState(it) }
            .launchIn(lifecycleScope)

        viewModel.effects().onEach {
            when (it) {
                MainViewEffect.EnableAccessibilityService -> showServiceNotEnabledWarning()
            }
        }.launchIn(lifecycleScope)
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
                viewBinding!!.relearnMainProgress.visibility = View.VISIBLE
                viewBinding!!.relearnPager.visibility = View.GONE
                viewBinding!!.emptyImage.visibility = View.GONE
                viewBinding!!.emptyText.visibility = View.GONE
                viewBinding!!.fab.visibility = View.GONE
                viewBinding!!.relearnPager.adapter = null
            }
            is MainViewState.Loaded -> {
                viewBinding!!.refresh.isRefreshing = false
                viewBinding!!.relearnMainProgress.visibility = View.GONE
                viewBinding!!.emptyImage.visibility = View.GONE
                viewBinding!!.emptyText.visibility = View.GONE

                if (viewState.sourceCount <= MIN_SOURCES_COUNT) {
                    showEmptyState(viewState)
                    viewBinding!!.fab.visibility = View.GONE
                } else if (!isAlreadyBound(viewState)) {
                    viewBinding!!.relearnPager.visibility = View.VISIBLE
                    setupViewPager(viewState)
                    updateFabVisibility()
                }
            }
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
        viewBinding!!.emptyText.visibility = View.VISIBLE
        viewBinding!!.relearnPager.visibility = View.GONE
        viewBinding!!.emptyImage.apply {
            visibility = View.VISIBLE
            val anim = AnimatedTumbleweed(context)
            setImageDrawable(anim)
            anim.start()
        }
    }

    private fun setupViewPager(viewState: MainViewState.Loaded) {
        val relearnAdapter = relearnAdapterFactory.create(viewState.sourceCount)

        if (viewBinding!!.relearnPager.adapter == null) {
            viewBinding!!.relearnPager.apply {
                adapter = relearnAdapter
                setCurrentItem(viewState.page, false)

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        if (position != (viewModel.currentState as? MainViewState.Loaded)?.page) {
                            viewModel.onPageChanged(position)
                        }
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
            viewBinding!!.relearnPager.adapter!!.notifyDataSetChanged()
        }

        relearnAdapter.effectsLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ReLearnAdapterEffect.LaunchReLearnEffect -> {
                    relearnLauncher.launch(it.reLearnTranslation)
                }
                is ReLearnAdapterEffect.ShowNextReLearnEffect -> {
                    viewBinding!!.relearnPager.setCurrentItem(relearnAdapter.itemCount - 1, true)
                }
                is ReLearnAdapterEffect.ReLearnDeletedEffect -> {
                    onReLearnDeleted(it.relearn, it.position)
                }
            }
        })
    }

    private fun animateShowFab(){
        ObjectAnimator.ofFloat(
            viewBinding!!.fab,
            "translationY",
            requireContext().dpToPx(80),
            0F
        ).apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationStart(animation: Animator?) {
                    viewBinding!!.fab.visibility = View.VISIBLE
                }
            })
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun animateHideFab(){
        ObjectAnimator.ofFloat(
            viewBinding!!.fab,
            "translationY",
            0F,
            requireContext().dpToPx(80)
        ).apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    viewBinding!!.fab.visibility = View.GONE
                }
            })
            interpolator = AccelerateInterpolator()
            start()
        }
    }

    private fun updateFabVisibility() {
        val visible = viewBinding!!.relearnPager.currentItem < viewBinding!!.relearnPager.adapter!!.itemCount - 1
        if(visible && viewBinding!!.fab.visibility != View.VISIBLE) {
            animateShowFab()
        } else if(!visible && viewBinding!!.fab.visibility != View.GONE){
            animateHideFab()
        }
    }

    private fun onReLearnDeleted(reLearnTranslation: ReLearnTranslation, position: Int) {
        Snackbar.make(
            viewBinding!!.main,
            R.string.message_relearn_deleted,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.action_undo) {
                (viewBinding?.relearnPager?.adapter as? ReLearnAdapter)?.undoReLearnDelete(
                    reLearnTranslation,
                    position
                )
                viewBinding?.relearnPager?.setCurrentItem(position, true)
            }.show()
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
