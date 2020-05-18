package com.azyoot.relearn.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.azyoot.relearn.R
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.databinding.FragmentMainBinding
import com.azyoot.relearn.di.ui.MainFragmentSubcomponent
import com.azyoot.relearn.service.common.ReLearnLauncher
import com.azyoot.relearn.service.worker.CheckAccessibilityServiceWorker
import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import javax.inject.Inject


@FlowPreview
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

    private val job = Job()
    private val coroutineScope = CoroutineScope(job)

    private val component: MainFragmentSubcomponent by lazy {
        (context!!.applicationContext as ReLearnApplication).appComponent
            .mainFragmentSubcomponentFactory()
            .create(coroutineScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.complete()
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

        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer {
            bindState(it)
        })
    }

    private fun bindState(viewState: MainViewState) {
        when (viewState) {
            is MainViewState.Loading, is MainViewState.Initial -> {
                viewBinding!!.relearnMainProgress.visibility = View.VISIBLE
                viewBinding!!.relearnPager.visibility = View.GONE
            }
            is MainViewState.Loaded -> {
                viewBinding!!.refresh.isRefreshing = false
                viewBinding!!.relearnMainProgress.visibility = View.GONE
                viewBinding!!.relearnPager.visibility = View.VISIBLE
                setupViewPager(viewState)

                if (!viewState.isServiceEnabled) {
                    showServiceNotEnabledWarning()
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

    private fun isViewPagerSetup() = viewBinding!!.relearnPager.adapter != null

    private fun setupViewPager(viewState: MainViewState.Loaded) {
        val relearnAdapter = relearnAdapterFactory.create(viewState.sourceCount)

        viewBinding!!.relearnPager.apply {
            adapter = relearnAdapter
            setCurrentItem(relearnAdapter.itemCount - 1, false)
        }

        relearnAdapter.actionsLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ReLearnAdapterActions.LaunchReLearn -> {
                    relearnLauncher.launch(it.reLearnTranslation)
                }
                is ReLearnAdapterActions.ShowNextReLearn -> {
                    viewBinding!!.relearnPager.setCurrentItem(relearnAdapter.itemCount - 1, true)
                }
            }
        })
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
