package com.azyoot.relearn.service.common

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.azyoot.relearn.domain.entity.ReLearnTranslation
import com.azyoot.relearn.domain.entity.SourceType
import com.azyoot.relearn.ui.relearn.ReLearnLaunchUrlActivity
import com.azyoot.relearn.util.UrlProcessing
import timber.log.Timber
import javax.inject.Inject

class ReLearnLauncher @Inject constructor(
    private val applicationContext: Context,
    private val urlProcessing: UrlProcessing
) {
    fun launch(reLearnTranslation: ReLearnTranslation) {
        when (reLearnTranslation.source.sourceType) {
            SourceType.WEBPAGE_VISIT -> launchUrl(
                reLearnTranslation.source.webpageVisit?.url ?: return
            )
            SourceType.TRANSLATION -> launchTranslation(reLearnTranslation.sourceText)
        }
    }

    fun launchUrl(url: String) {
        if (urlProcessing.isValidUrl(urlProcessing.ensureStartsWithHttpsScheme(url)).not()) {
            Timber.w("Invalid url $url")
            return
        }

        Timber.d("Launching url for relearn $url")

        val intent = Intent(applicationContext, ReLearnLaunchUrlActivity::class.java).apply {
            putExtra(
                ReLearnLaunchUrlActivity.EXTRA_URL,
                urlProcessing.ensureStartsWithHttpsScheme(url)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        applicationContext.startActivity(intent)
    }

    fun launchTranslation(text: String) {
        if (Build.VERSION.SDK_INT >= 29) {
            val intent = Intent(Intent.ACTION_TRANSLATE).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                applicationContext.startActivity(intent)
                return
            } catch (ex: ActivityNotFoundException) {
                Timber.w(ex, "No activity found to handle ACTION_TRANSLATE")
            }
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            component = ComponentName(
                "com.google.android.apps.translate",
                "com.google.android.apps.translate.TranslateActivity"
            )
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra("key_text_input", text)
            putExtra("key_suggest_translation", "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            applicationContext.startActivity(sendIntent)
            return
        } catch (ex: ActivityNotFoundException) {
            Timber.w(ex, "No activity found to handle ACTION_SEND")
        }
    }
}