package com.azyoot.relearn.ui.main

import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.azyoot.relearn.R
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.databinding.MainFragmentBinding
import com.azyoot.relearn.service.di.MainFragmentSubcomponent
import com.azyoot.relearn.service.worker.WebpageDownloadWorker
import kotlinx.coroutines.FlowPreview
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@FlowPreview
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }
    private var viewBinding: MainFragmentBinding? = null

    private val component: MainFragmentSubcomponent by lazy {
        (context!!.applicationContext as ReLearnApplication).appComponent.mainFragmentSubcomponent()
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
        MainFragmentBinding.inflate(LayoutInflater.from(context)).let { binding ->
            this.viewBinding = binding
            binding.root
        }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rescheduleWebpageDownloadJob()

        viewModel.isMonitoringServiceEnabled.observe(viewLifecycleOwner, Observer { isEnabled ->
            if (!isEnabled) {
                AlertDialog.Builder(activity)
                    .setMessage(R.string.dialog_enable_accessibility_service)
                    .setNegativeButton(android.R.string.no) { _, _ -> }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    }
                    .show()
            }
        })

        val historyAdapter = WebpageVisitAdapter(context!!)
        viewBinding!!.listHistory.adapter = historyAdapter
        viewBinding!!.listHistory.layoutManager = LinearLayoutManager(context)
        viewModel.history.observe(viewLifecycleOwner, Observer {
            historyAdapter.submitList(it)
        })
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkMonitoringService()
    }

    private fun rescheduleWebpageDownloadJob() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<WebpageDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context!!)
            .enqueueUniqueWork(WebpageDownloadWorker.NAME, ExistingWorkPolicy.KEEP, request)
    }
}
