package com.azyoot.relearn.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.azyoot.relearn.R
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.di.MainFragmentSubcomponent
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@FlowPreview
class MainFragment : Fragment(R.layout.main_fragment) {

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private val component: MainFragmentSubcomponent by lazy {
        (context!!.applicationContext as ReLearnApplication).appComponent.mainFragmentSubcomponent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkMonitoringService()
    }
}
