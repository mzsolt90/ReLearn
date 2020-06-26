package com.azyoot.relearn.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.azyoot.relearn.R
import com.azyoot.relearn.ReLearnApplication
import com.azyoot.relearn.databinding.FragmentOnboardingGeneralBinding
import com.azyoot.relearn.di.ui.OnboardingFragmentSubcomponent
import com.azyoot.relearn.service.MonitoringService
import com.azyoot.relearn.ui.main.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*

@ExperimentalCoroutinesApi
@FlowPreview
class OnboardingFragment : Fragment() {
    private var viewBinding: FragmentOnboardingGeneralBinding? = null

    private lateinit var params: OnboardingFragmentParams

    private val component: OnboardingFragmentSubcomponent by lazy {
        (context!!.applicationContext as ReLearnApplication).appComponent
            .onboardingFragmentSubcomponentFactory()
            .create(params)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val mainViewModel: MainViewModel
        get() = ViewModelProvider(parentFragment!!).get(MainViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        params = arguments?.getParcelable(EXTRA_PARAMS)
            ?: throw IllegalArgumentException("Missing params")

        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentOnboardingGeneralBinding.inflate(inflater, container, false).let {
        viewBinding = it
        it.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding!!.apply {
            title.setText(titleResId)
            message.setText(messageResId)
            icon.setImageResource(iconResId)

            next.setText(buttonResId)
            next.setOnClickListener {
                if (params.screen == OnboardingScreen.ENABLE_ACCESSIBILITY) {
                    goToAccessibilitySettings()
                } else {
                    mainViewModel.onOnboardingScreenNext(params.screen)
                }
            }

            closeButton.visibility = if (params.screen in EnumSet.of(
                    OnboardingScreen.ENABLE_ACCESSIBILITY,
                    OnboardingScreen.HOW_IT_WORKS
                )
            ) View.VISIBLE else View.GONE
            closeButton.setOnClickListener {
                mainViewModel.onOnboardingScreenClosed(params.screen)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (params.screen == OnboardingScreen.ENABLE_ACCESSIBILITY && MonitoringService.isRunning(
                context!!
            )
        ) {
            mainViewModel.onOnboardingScreenNext(params.screen)
        }
    }

    private fun goToAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    @get:StringRes
    private val titleResId: Int
        get() = when (params.screen) {
            OnboardingScreen.WELCOME -> R.string.onboarding_welcome_title
            OnboardingScreen.HOW_IT_WORKS -> R.string.onboarding_how_it_works_title
            OnboardingScreen.ENABLE_ACCESSIBILITY -> R.string.onboarding_enable_title
            OnboardingScreen.DONE -> R.string.onboarding_done_title
        }

    @get:StringRes
    private val messageResId: Int
        get() = when (params.screen) {
            OnboardingScreen.WELCOME -> R.string.onboarding_welcome_message
            OnboardingScreen.HOW_IT_WORKS -> R.string.onboarding_how_it_works_message
            OnboardingScreen.ENABLE_ACCESSIBILITY -> R.string.onboarding_enable_message
            OnboardingScreen.DONE -> R.string.onboarding_done_message
        }

    @get:StringRes
    private val buttonResId: Int
        get() = when (params.screen) {
            OnboardingScreen.WELCOME -> R.string.button_get_started
            OnboardingScreen.HOW_IT_WORKS -> R.string.button_next
            OnboardingScreen.ENABLE_ACCESSIBILITY -> R.string.button_enable
            OnboardingScreen.DONE -> R.string.button_done
        }

    @get:DrawableRes
    private val iconResId: Int
        get() = when (params.screen) {
            OnboardingScreen.WELCOME -> R.drawable.ic_logo_standalone
            OnboardingScreen.HOW_IT_WORKS -> R.drawable.ic_logo_standalone
            OnboardingScreen.ENABLE_ACCESSIBILITY -> R.drawable.ic_logo_standalone
            OnboardingScreen.DONE -> R.drawable.ic_logo_standalone
        }

    fun getParams() = params

    companion object {
        private const val EXTRA_PARAMS = "params"
        const val TAG = "OnboardingFragment"

        fun newInstance(params: OnboardingFragmentParams) =
            OnboardingFragment().apply {
                arguments = Bundle().apply { putParcelable(EXTRA_PARAMS, params) }
            }
    }
}